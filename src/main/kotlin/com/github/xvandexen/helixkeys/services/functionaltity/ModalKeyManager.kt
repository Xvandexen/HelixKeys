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

typealias KeyBindingMap = Map<KeyCombo, RecKeyBinding>

@Service(Service.Level.PROJECT)
class ModalKeyManager(
  private val project: Project,
) : Disposable {

  private val keybindings: MutableMap<ModeManager.Mode, KeyBindingMap> = KeyBindingConfig.loadConfig()

  private val specialKeyMap: Map<Int, Char> = mapOf(
    VK_UP to '\uE000',
    VK_DOWN to '\uE001',
    VK_LEFT to '\uE002',
    VK_RIGHT to '\uE003',
    VK_END to '\uE004',
    VK_HOME to '\uE005'
  )



  private var keyInterruptActive = true
  private var repeatCommandForThis: String = ""
  private val modeManager = ModeManager.getInstance(project)
  private val overlayService = CommandOverlayService.getInstance(project)
  private val commandExecutor: CommandExecutor = CommandExecutor.getInstance(project)
  private val normalModeBindings: KeyBindingMap = keybindings[ModeManager.Mode.NORMAL] ?: error("No bindings for NORMAL mode")
  private val insertModeBindings: KeyBindingMap = keybindings[ModeManager.Mode.INSERT] ?: error("No bindings for INSERT mode")
  private val selectModeBindings: KeyBindingMap = keybindings[ModeManager.Mode.SELECT] ?: error("No bindings for SELECT mode")
  private var activeKeyCordMap: KeyBindingMap = mutableMapOf()
  private var modifiers = KeyBindingConfig.Modifiers()


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

  private fun typedInBindings(keyChar: Char): Boolean {
    val keyCombo = KeyCombo(keyChar, modifiers)
    return activeKeyCordMap.containsKey(keyCombo)
  }


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

  private fun handleSubMenu(subBindings: Map<KeyCombo, RecKeyBinding>) {
    // Implementation for handling submenus with the new structure
    activeKeyCordMap = subBindings
    overlayService.showBindings(subBindings)

  }






  override fun dispose() {
    overlayService.hideBindings()
    Disposer.dispose(this)
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): ModalKeyManager = project.service()
  }

}