package com.github.xvandexen.helixkeys.functionaltity

import com.github.xvandexen.helixkeys.commands.CommandExecutor
import com.github.xvandexen.helixkeys.configuration.KeyBindingConfig.RecKeyBinding
import com.github.xvandexen.helixkeys.ui.UiHandler
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import java.awt.event.KeyEvent

typealias KeyBindingMap = Map<Set<Int>, RecKeyBinding>

class ModalKeyManager(
  private val project: Project,
  private val keybindings: MutableMap<ModeManager.Mode, Map<Set<Int>, RecKeyBinding>>
) : Disposable {


  private val normalModeBindings: KeyBindingMap =  keybindings[ModeManager.Mode.NORMAL] ?: error("No bindings for NORMAL mode")
  private val insertModeBindings: KeyBindingMap = keybindings[ModeManager.Mode.INSERT] ?: error("No bindings for NORMAL mode")
  private val visualModeBindings: KeyBindingMap = mutableMapOf()
  private var activeKeyCordMap: KeyBindingMap = mutableMapOf()
  private var activeModifiers: MutableSet<Int> = mutableSetOf()


  init {
    activeKeyCordMap = insertModeBindings
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
      KeyEvent.KEY_PRESSED -> return handleKeyPress(event.keyCode)
      KeyEvent.KEY_RELEASED -> return handlekeyRelease(event.keyCode)
      KeyEvent.KEY_TYPED -> return typedInBindings(event.keyChar)
    }

    return false
  }

  private fun typedInBindings(keyChar: Char): Boolean {
    val heldKeys = buildSet { add(KeyEvent.getExtendedKeyCodeForChar(keyChar.code)); addAll(activeModifiers) }
    return (normalModeBindings.containsKey(heldKeys) || insertModeBindings.containsKey(heldKeys))
    //TODO("Find Better Way")
  }

  private fun handleKeyPress(keycode: Int): Boolean {
    val heldKeys = buildSet { add(keycode); addAll(activeModifiers) }
    thisLogger().info("heldKeys. = $heldKeys")

    when (keycode) {
      KeyEvent.VK_SHIFT -> activeModifiers.add(KeyEvent.VK_SHIFT)
      KeyEvent.VK_CONTROL -> activeModifiers.add(KeyEvent.VK_CONTROL)
      KeyEvent.VK_ALT -> activeModifiers.add(KeyEvent.VK_ALT)
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
    activeKeyCordMap = when (ModeManager.currentMode) {
      ModeManager.Mode.NORMAL -> normalModeBindings
      ModeManager.Mode.INSERT -> insertModeBindings
    }
    thisLogger().info("Command Found[$command], Reseting activeKeyCordMap to = $activeKeyCordMap")

    CommandExecutor.executeCommand(command)

  }

  private fun handleSubMenu(subBindings: Map<Set<Int>, RecKeyBinding>) {
    UiHandler.displayMenu(subBindings)
    activeKeyCordMap= subBindings
  }

  private fun handlekeyRelease(keycode: Int): Boolean {
    thisLogger().info("Key Released $keycode" )
    return when (keycode) {
      KeyEvent.VK_SHIFT, KeyEvent.VK_ALT, KeyEvent.VK_CONTROL -> {
        activeModifiers.remove(keycode); true
      }

      else -> false
    }

  }






  override fun dispose() {
    TODO("Not yet implemented")
  }


}