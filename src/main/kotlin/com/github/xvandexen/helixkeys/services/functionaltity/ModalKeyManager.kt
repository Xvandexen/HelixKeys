package com.github.xvandexen.helixkeys.services.functionaltity

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig.RecKeyBinding
import com.github.xvandexen.helixkeys.services.ui.UiHandler
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*

typealias KeyBindingMap = Map<Set<Int>, RecKeyBinding>

class ModalKeyManager(
  private val project: Project,
  private val keybindings: MutableMap<ModeManager.Mode, Map<Set<Int>, RecKeyBinding>>
) : Disposable {


  private var repeatCommandForThis: String = ""
  private val modeManager = ModeManager.getInstance(project)
  private val uIHandler = UiHandler.getInstance(project)
  private val commandExecutor: CommandExecutor = CommandExecutor.getInstance(project)
  private val normalModeBindings: KeyBindingMap =  keybindings[ModeManager.Mode.NORMAL] ?: error("No bindings for NORMAL mode")
  private val insertModeBindings: KeyBindingMap = keybindings[ModeManager.Mode.INSERT] ?: error("No bindings for NORMAL mode")
  private val visualModeBindings: KeyBindingMap = mutableMapOf()
  private var activeKeyCordMap: KeyBindingMap = mutableMapOf()
  private var activeModifiers: MutableSet<Int> = mutableSetOf()


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
      KEY_PRESSED -> return handleKeyPress(event.keyCode)
      KEY_RELEASED -> return handlekeyRelease(event.keyCode)
      KEY_TYPED -> return typedInBindings(event.keyChar)
    }

    return false
  }

  private fun typedInBindings(keyChar: Char): Boolean {
    val heldKeys = buildSet { add(getExtendedKeyCodeForChar(keyChar.code)); addAll(activeModifiers) }
    thisLogger().info("Typed Char = $keyChar + ${keyChar.code}" )

    return when (modeManager.currentMode) {
      ModeManager.Mode.NORMAL -> true
      ModeManager.Mode.INSERT -> insertModeBindings.containsKey(heldKeys)
    }
  }

  private fun handleKeyPress(keycode: Int): Boolean {
    val heldKeys = buildSet { add(keycode); addAll(activeModifiers) }
    thisLogger().info("heldKeys = $heldKeys")

    when (keycode) {
      VK_SHIFT -> activeModifiers.add(VK_SHIFT)
      VK_CONTROL -> activeModifiers.add(VK_CONTROL)
      VK_ALT -> activeModifiers.add(VK_ALT)

      VK_1, VK_2, VK_3, VK_4, VK_5, VK_6, VK_7,VK_8,VK_9, VK_0,
      VK_NUMPAD1, VK_NUMPAD2, VK_NUMPAD3, VK_NUMPAD4,VK_NUMPAD5,VK_NUMPAD6, VK_NUMPAD7,VK_NUMPAD8,VK_NUMPAD9, VK_NUMPAD0
        -> repeatCommandForThis += keycode.toChar()

      else -> {
        thisLogger().info("""
          |Else Branch active
          |Held Keys = $heldKeys
          |Active KeyCord Map = $activeKeyCordMap
          |Normal Binding = $normalModeBindings
          |Insert Bindings = $insertModeBindings
        """.trimMargin())

            val binding: RecKeyBinding? = activeKeyCordMap[heldKeys]
            thisLogger().info("Found Binding for $heldKeys, Binding is $binding")
            return if (binding != null) {
              when {
                binding.command != null -> attemptCommand(binding.command)
                binding.subBindings != null -> handleSubMenu(binding.subBindings)
              }
              true
            } else false
          }

    }
    return false
  }

  private fun attemptCommand(command: CommandExecutor.HelixCommand) {
    if (repeatCommandForThis != ""){
      commandExecutor.executeCommandXTimes(repeatCommandForThis,command)
    }
    else{
    commandExecutor.executeCommand(command)}
    repeatCommandForThis = ""
    activeKeyCordMap = when (modeManager.currentMode) {
      ModeManager.Mode.NORMAL -> normalModeBindings
      ModeManager.Mode.INSERT -> insertModeBindings
    }
    uIHandler.closeMenu()
    thisLogger().info("Command Found[$command], Reseting activeKeyCordMap to = $activeKeyCordMap")


  }

  private fun handleSubMenu(subBindings: Map<Set<Int>, RecKeyBinding>) {
    uIHandler.displayMenu(subBindings)
    activeKeyCordMap= subBindings
    repeatCommandForThis= ""
  }

  private fun handlekeyRelease(keycode: Int): Boolean {
    thisLogger().info("Key Released $keycode" )
    return when (keycode) {
      VK_SHIFT, VK_ALT, VK_CONTROL -> {
        activeModifiers.remove(keycode); true
      }

      else -> false
    }

  }






  override fun dispose() {
    Disposer.dispose(this)
  }


}