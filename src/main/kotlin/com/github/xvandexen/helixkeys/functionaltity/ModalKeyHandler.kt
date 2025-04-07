package com.github.xvandexen.helixkeys.functionaltity

import com.github.xvandexen.helixkeys.configuration.KeybindingConfig
import com.intellij.ide.DataManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import java.awt.event.KeyEvent
import java.util.LinkedHashSet
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

class ModalKeyHandler(
  private val modeManager: ModeManager,
  private val project: Project,
  private val keybindings: Map<String, KeybindingConfig.RecKeyBinding>,


  ) : Disposable {
  private val logger = thisLogger()

  // Map key combinations to action IDs for each mode
  private val normalModeKeyMaps = hashMapOf<Set<Int>, String>()
  private val specialModeKeyMaps = hashMapOf<Set<Int>, String>()

  // Use LinkedHashSet for order preservation (FIFO)
  private val pressedKeys = LinkedHashSet<Int>()

  // Lock for thread-safe access to the ordered set
  private val keysLock = ReentrantReadWriteLock()

  init {
    // Setup key mappings for each mode
    setupKeyMappings()


    //Listens for and dispatches Keyevents, all other events continue as normal
    //Does not interupt typed char
    IdeEventQueue.getInstance().addDispatcher({ event ->
      if (event is KeyEvent){
      handleEvent(event)
      } else{
        false
      }

    }, this)


    logger.info("ModalKeyHandler initialized, mode: ${modeManager.currentMode}")
  }


  private fun setupKeyMappings() {
    // Normal mode mappings
    normalModeKeyMaps[setOf(KeyEvent.VK_ESCAPE)] = "SwitchToSpecialMode"

    // Special mode mappings
    specialModeKeyMaps[setOf(KeyEvent.VK_ESCAPE)] = "SwitchToNormalMode"
    specialModeKeyMaps[setOf(KeyEvent.VK_D)] = "SwitchToNormalMode"
    specialModeKeyMaps[setOf(KeyEvent.VK_E)] = "EditorUp"
    specialModeKeyMaps[setOf(KeyEvent.VK_O)] = "EditorDown"

    // Multi-key combinations
    specialModeKeyMaps[setOf(KeyEvent.VK_CONTROL, KeyEvent.VK_K)] = "EditorScrollUp"
    specialModeKeyMaps[setOf(KeyEvent.VK_CONTROL, KeyEvent.VK_J)] = "EditorScrollDown"
  }

  private fun handleEvent(event: Any): Boolean {
    if (event !is KeyEvent) return false
    logger.info("KeyEvent: ${event.id} ")
    when (event.id) {
      KeyEvent.KEY_PRESSED -> return handleKeyPressed(event)
      KeyEvent.KEY_RELEASED -> return handleKeyReleased(event)
      KeyEvent.KEY_TYPED -> return hasKeyBinding(event);
    }

    return false
  }

  private fun hasKeyBinding(event: KeyEvent): Boolean {
    //TODO(find A better solution for this)
    val char = event.keyChar
    logger.info("Char typed : $char")
    val setChar = setOf(KeyEvent.getExtendedKeyCodeForChar(char.code))
    logger.info("setChar: $setChar")

    return normalModeKeyMaps.containsKey(setChar) || (specialModeKeyMaps.containsKey(setChar) && modeManager.currentMode == ModeManager.Mode.Insert)
  }

  private fun handleKeyPressed(event: KeyEvent): Boolean {
    val keyCode = event.keyCode

    keysLock.write {
      // Ignore auto-repeats (when holding a key down)
      if (pressedKeys.contains(keyCode)) {

        return@write
      }

      // Add key to the set in order
      pressedKeys.add(keyCode)
      logger.info("Key pressed: $keyCode, keys: $pressedKeys")
    }

    // In SPECIAL mode, always consume the event
    // In NORMAL mode, only consume if we have a mapping for it
    return when {
      isInSpecialMode() -> {
        event.consume()
        true
      }
      hasNormalModeMapping(keyCode) -> {
        event.consume()
        true
      }
      else -> false
    }
  }

  private fun handleKeyReleased(event: KeyEvent): Boolean {
    logger.info("Key Released Event: $event")
    val keyCode = event.keyCode
    var currentKeys = setOf<Int>() // Initialize with empty set
    var releasedKeyPresent = false

    keysLock.write {
      // Check if the key was in our set
      releasedKeyPresent = pressedKeys.contains(keyCode)
      if (!releasedKeyPresent) {
        return@write
      }

      // Make a copy of the current key set before removing the released key
      currentKeys = pressedKeys.toSet()

      // Remove the released key
      pressedKeys.remove(keyCode)

      logger.info("Key released: $keyCode, remaining keys: $pressedKeys")
    }

    // If we had the key in our set, check for a command
    if (releasedKeyPresent) {

      executeActionForKeys(currentKeys)
    }

    return false // Don't consume key release events
  }

  private fun isInSpecialMode(): Boolean {
    return modeManager.currentMode == ModeManager.Mode.Insert
  }

  private fun hasNormalModeMapping(keyCode: Int): Boolean {
    // Check if we have a mapping for this key in normal mode
    val singleKeySet = setOf(keyCode)
    return normalModeKeyMaps.containsKey(singleKeySet) ||
            normalModeKeyMaps.keys.any { it.contains(keyCode) }
  }

  private fun executeActionForKeys(keys: Set<Int>) {
    if (keys.isEmpty()) return

    logger.info("Checking for action with keys: $keys")

    val currentMode = modeManager.currentMode
    val keyMap = when (currentMode) {
      ModeManager.Mode.NORMAL -> normalModeKeyMaps
      ModeManager.Mode.Insert -> specialModeKeyMaps
    }

    // Find the action for this key combination
    val actionId = keyMap[keys]

    if (actionId != null) {
      logger.info("Found mapping for keys $keys: $actionId")

      // Handle special actions
      when (actionId) {
        "SwitchToSpecialMode" -> modeManager.toggleMode(ModeManager.Mode.Insert)
        "SwitchToNormalMode" -> modeManager.toggleMode(ModeManager.Mode.NORMAL)
        else -> executeAction(actionId)
      }
    } else {
      logger.info("No mapping found for keys: $keys")
    }
  }

  private fun executeAction(actionId: String) {
    logger.info("Executing action: $actionId")

    val action = ActionManager.getInstance().getAction(actionId) ?: run {
      logger.error("Action not found: $actionId")
      return
    }

    try {
      // Get data context from current focus
      val dataContext = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(500)
        ?: DataContext { dataId ->
          when {
            PlatformDataKeys.PROJECT.`is`(dataId) -> project
            // You might need to add more context data here for specific actions
            else -> null
          }
        }

      val event = AnActionEvent.createFromAnAction(
        action,
        null,
        "ModalKeyHandler",
        dataContext
      )

      logger.info("Attempting Action $action")

      // Execute the action on the UI thread
      ApplicationManager.getApplication().invokeLater {
        if (ActionUtil.lastUpdateAndCheckDumb(action, event, false)) {
          ActionUtil.performActionDumbAwareWithCallbacks(action, event)
        } else {
          logger.warn("Action ${action.templateText} is not enabled in current context")
        }
      }
    } catch (e: Exception) {
      logger.error("Error executing action $actionId", e)
    }
  }

  override fun dispose() {
    logger.info("ModalKeyHandler disposed")
    // Clean up resources if needed
  }
}