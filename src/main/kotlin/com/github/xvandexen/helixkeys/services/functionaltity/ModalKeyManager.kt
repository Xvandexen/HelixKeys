package com.github.xvandexen.helixkeys.services.functionaltity

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig
import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig.RecKeyBinding
import com.github.xvandexen.helixkeys.services.configuration.KeyCombo
import com.github.xvandexen.helixkeys.services.ui.UiHandler
import com.intellij.codeInsight.codeVision.editorLensContextKey
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.jetbrains.rd.generator.nova.PredefinedType
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*

typealias KeyBindingMap = Map<KeyCombo, RecKeyBinding>

class ModalKeyManager(
  private val project: Project,
  private val keybindings: MutableMap<ModeManager.Mode, KeyBindingMap>
) : Disposable {

  private val specialKeyMap: Map<Int, Char> = mapOf(
    VK_UP to '\uE000',
    VK_DOWN to '\uE001',
    VK_LEFT to '\uE002',
    VK_RIGHT to '\uE003',
    VK_END to '\uE004',
    VK_HOME to '\uE005'
  )


  private var repeatCommandForThis: String = ""
  private val modeManager = ModeManager.getInstance(project)
  private val uIHandler = UiHandler.getInstance(project)
  private val commandExecutor: CommandExecutor = CommandExecutor.getInstance(project)
  private val normalModeBindings: KeyBindingMap = keybindings[ModeManager.Mode.NORMAL] ?: error("No bindings for NORMAL mode")
  private val insertModeBindings: KeyBindingMap = keybindings[ModeManager.Mode.INSERT] ?: error("No bindings for NORMAL mode")
  private val visualModeBindings: KeyBindingMap = mutableMapOf()
  private var activeKeyCordMap: KeyBindingMap = mutableMapOf()
  private var modifiers = KeyBindingConfig.Modifiers()


  init {

    activeKeyCordMap = normalModeBindings
    IdeEventQueue.getInstance().addDispatcher({ event ->
      if (event is KeyEvent) {
        handleEvent(event)
      } else {
        false
      }

    }, this)

  }

  private fun handleEvent(event: Any): Boolean {
    if (event !is KeyEvent) return false
    when (event.id) {
      KEY_PRESSED -> return handleKeyPress(event)
      KEY_RELEASED -> return handlekeyRelease(event)
      KEY_TYPED -> return typedInBindings(event.keyChar)
    }

    return false
  }


  private fun handleKeyPress(keyEvent: KeyEvent): Boolean {

    var keyChar = keyEvent.keyChar

    when (keyEvent.keyCode) {
      VK_CONTROL -> {modifiers.control = true; return true}
      VK_ALT -> {modifiers.meta = true; return true}
      VK_UP, VK_LEFT, VK_DOWN, VK_RIGHT, VK_END, VK_HOME -> keyChar = specialKeyMap[keyEvent.keyCode]!!
    }

    thisLogger().info("KeyPressed = Char: $keyChar")
    val keyCombo = KeyCombo(keyChar, modifiers)

    // Look up the key in the current active bindings
    val binding = activeKeyCordMap[keyCombo]

    if (binding != null) {
      thisLogger().info("Binding Found = Binding: $binding")
      binding.command?.let { attemptCommand(it) }
      binding.subBindings?.let { handleSubMenu(it) }
      return true
    }

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
    }
    uIHandler.closeMenu()
    thisLogger().info("Command Found[$command], Reseting activeKeyCordMap to = $activeKeyCordMap")


  }

  private fun handleSubMenu(subBindings: Map<Char, RecKeyBinding>) {
    // Implementation for handling submenus with the new structure
    activeKeyCordMap = subBindings.mapKeys { (char, binding) ->
      KeyCombo(char, binding.modifiers ?: KeyBindingConfig.Modifiers())
    }
    uIHandler.displayMenu(subBindings)
  }


  private fun handlekeyRelease(keyEvent: KeyEvent): Boolean {
    val keycode = keyEvent.keyCode
    when (keycode) {
      VK_CONTROL -> modifiers.control = false
      VK_ALT -> modifiers.control = true

    }

    return true

  }






  override fun dispose() {
    Disposer.dispose(this)
  }


}