package com.github.xvandexen.helixkeys.services.functionaltity

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig
import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig.RecKeyBinding
import com.github.xvandexen.helixkeys.services.configuration.KeyCombo
import com.github.xvandexen.helixkeys.services.ui.CommandOverlayService
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.IdeFocusManager
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*

/**
 * Type alias for a map of key combinations to their corresponding key bindings.
 * 
 * This simplifies the representation of key binding maps for different editor modes.
 */
typealias KeyBindingMap = Map<KeyCombo, RecKeyBinding>

/**
 * Service responsible for managing key bindings in different editor modes.
 * 
 * This class intercepts key events, processes them based on the current editor mode
 * (normal, insert, select), and executes the appropriate commands when key combinations
 * match configured bindings. It also handles sub-menus for complex key sequences and
 * updates the command overlay UI to show available bindings.
 *
 * @property project The current project instance
 */
@Service(Service.Level.PROJECT)
class ModalKeyManager(
  private val project: Project,
) : Disposable {

  /**
   * Map of editor modes to their corresponding key binding maps.
   * Loaded from the configuration file.
   */
  private val keybindings: MutableMap<ModeManager.Mode, KeyBindingMap> = KeyBindingConfig.loadConfig()

  /**
   * Map of special key codes to their character representations.
   * This allows handling of arrow keys and navigation keys that don't have standard character representations.
   */
  private val specialKeyMap: Map<Int, Char> = mapOf(
    VK_UP to '\uE000',
    VK_DOWN to '\uE001',
    VK_LEFT to '\uE002',
    VK_RIGHT to '\uE003',
    VK_END to '\uE004',
    VK_HOME to '\uE005'
  )

  /**
   * Flag indicating whether key event interception is active.
   */
  private var keyInterruptActive = true

  /**
   * String representing the number of times to repeat a command.
   * Used for commands like "3j" to move down 3 lines.
   */
  private var repeatCommandForThis: String = ""

  /**
   * Reference to the ModeManager service for tracking the current editor mode.
   */
  private val modeManager = ModeManager.getInstance(project)

  /**
   * Reference to the CommandOverlayService for displaying available key bindings.
   */
  private val overlayService = CommandOverlayService.getInstance(project)

  /**
   * Reference to the CommandExecutor service for executing Helix commands.
   */
  private val commandExecutor: CommandExecutor = CommandExecutor.getInstance(project)

  /**
   * Key bindings for normal mode.
   */
  private val normalModeBindings: KeyBindingMap = keybindings[ModeManager.Mode.NORMAL] ?: error("No bindings for NORMAL mode")

  /**
   * Key bindings for insert mode.
   */
  private val insertModeBindings: KeyBindingMap = keybindings[ModeManager.Mode.INSERT] ?: error("No bindings for INSERT mode")

  /**
   * Key bindings for select mode.
   */
  private val selectModeBindings: KeyBindingMap = keybindings[ModeManager.Mode.SELECT] ?: error("No bindings for SELECT mode")

  /**
   * The currently active key binding map.
   * Changes based on the current mode and active sub-menu.
   */
  private var activeKeyCordMap: KeyBindingMap = mutableMapOf()

  /**
   * The current key modifiers (Control, Meta, Alt).
   */
  private var modifiers = KeyBindingConfig.Modifiers()


  /**
   * Initializes the ModalKeyManager.
   * 
   * Sets up the initial active key binding map and adds a dispatcher to intercept key events.
   * Also logs the loaded key bindings for debugging purposes.
   */
  init {
    // Use Application.invokeLater to ensure UI is ready
    activeKeyCordMap = normalModeBindings

    IdeEventQueue.getInstance().addDispatcher({ event ->
      if (event is KeyEvent && keyInterruptActive) {
        handleEvent(event)
      } else {
        false
      }
    }, this)

    thisLogger().info("NormalBindings = $normalModeBindings")
    thisLogger().info("InsertBindings = $insertModeBindings")
    thisLogger().info("SelectBindings = $selectModeBindings")
  }

  /**
   * Handles an event from the IDE event queue.
   * 
   * This method filters for KeyEvents and processes them only if they occur
   * in an editor component. It delegates to specific handlers based on the
   * type of key event (pressed or typed).
   *
   * @param event The event to handle
   * @return True if the event was handled and should be consumed, false otherwise
   */
  private fun handleEvent(event: Any): Boolean {
    // First ensure it's a KeyEvent
    if (event !is KeyEvent) return false

    thisLogger().info("Entering KeyEvent event = $event")
    // Try to get the current editor directly from FileEditorManager

    val focusedComponent = IdeFocusManager.getInstance(project).focusOwner
    val editors = EditorFactory.getInstance().allEditors
    if (editors.none { it.contentComponent === focusedComponent }){
      return false
    }

    // Now we have an editor, process the key event
    return when (event.id) {
      KEY_PRESSED -> handleKeyPress(event)
      KEY_TYPED -> typedInBindings(event.keyChar)
      else -> false
    }
  }

  /**
   * Handles a key press event.
   * 
   * This method extracts the key character and modifiers from the event,
   * looks up the corresponding binding in the active key binding map,
   * and executes the command or activates the sub-menu if a binding is found.
   *
   * @param keyEvent The key press event to handle
   * @return True if the event was handled and should be consumed, false otherwise
   */
  private fun handleKeyPress(keyEvent: KeyEvent): Boolean {
    var keyChar = keyEvent.keyChar
    modifiers.meta = keyEvent.isMetaDown
    modifiers.alt = keyEvent.isAltDown
    modifiers.control = keyEvent.isControlDown


    this.thisLogger().info("Pre check event = $keyEvent, keyCharCode" + " = ${keyEvent.keyChar.code}, keyCode = ${keyEvent.keyCode}, Modifiers = $modifiers" )
    if(keyEvent.keyCode in listOf(VK_UP, VK_LEFT, VK_DOWN, VK_RIGHT, VK_END, VK_HOME)){
      keyChar = specialKeyMap[keyEvent.keyCode]!!
    }

    thisLogger().info("KeyPressed = Char: $keyChar")
    val keyCombo = KeyCombo(keyChar, modifiers)

    thisLogger().info("KeyCombo: $keyCombo")

    // Look up the key in the current active bindings
    val binding = activeKeyCordMap[keyCombo]

    thisLogger().info("Binding = $binding")
    if (binding != null) {
      thisLogger().info("Binding Found = Binding: $binding")
      binding.command?.let { attemptCommand(it) }
      binding.subBindings?.let { handleSubMenu(it) }
      return true
    }

    activeKeyCordMap = when (modeManager.currentMode){
      ModeManager.Mode.NORMAL -> normalModeBindings
      ModeManager.Mode.INSERT -> insertModeBindings
      ModeManager.Mode.SELECT -> selectModeBindings
    }
    overlayService.hideBindings()
    return false
  }

  /**
   * Checks if a typed character is in the active key binding map.
   * 
   * This method is called for KEY_TYPED events and determines if the
   * typed character (with current modifiers) is a valid key binding.
   *
   * @param keyChar The character that was typed
   * @return True if the character is in the active key binding map, false otherwise
   */
  private fun typedInBindings(keyChar: Char): Boolean {
    val keyCombo = KeyCombo(keyChar, modifiers)
    return activeKeyCordMap.containsKey(keyCombo)
  }

  /**
   * Executes a Helix command.
   * 
   * This method executes the specified command, handling command repetition
   * if a repeat count has been specified. After executing the command,
   * it resets the active key binding map to the current mode's bindings
   * and hides the command overlay.
   *
   * @param command The Helix command to execute
   */
  private fun attemptCommand(command: CommandExecutor.HelixCommand) {
    if (repeatCommandForThis != "") {
      commandExecutor.executeCommandXTimes(repeatCommandForThis, command)
    } else {
      commandExecutor.executeCommand(command)
    }
    repeatCommandForThis = ""
    activeKeyCordMap = when (modeManager.currentMode) {
      ModeManager.Mode.NORMAL -> normalModeBindings
      ModeManager.Mode.INSERT -> insertModeBindings
      ModeManager.Mode.SELECT -> selectModeBindings
    }
    overlayService.hideBindings()
    thisLogger().info("Command Found[$command], Reseting activeKeyCordMap to = $activeKeyCordMap")
  }

  /**
   * Activates a sub-menu for complex key sequences.
   * 
   * This method sets the active key binding map to the specified sub-bindings
   * and shows the command overlay with the available bindings in the sub-menu.
   *
   * @param subBindings The map of key bindings for the sub-menu
   */
  private fun handleSubMenu(subBindings: Map<KeyCombo, RecKeyBinding>) {
    // Implementation for handling submenus with the new structure
    activeKeyCordMap = subBindings
    overlayService.showBindings(subBindings)
  }

  /**
   * Disposes of resources used by this service.
   * 
   * This method is called when the service is being shut down.
   * It hides the command overlay and disposes of this service.
   */
  override fun dispose() {
    overlayService.hideBindings()
    Disposer.dispose(this)
  }

  /**
   * Companion object providing access to the ModalKeyManager service instance.
   */
  companion object {
    /**
     * Gets the ModalKeyManager service instance for the specified project.
     *
     * @param project The project for which to get the ModalKeyManager service
     * @return The ModalKeyManager service instance
     */
    @JvmStatic
    fun getInstance(project: Project): ModalKeyManager = project.service()
  }

}
