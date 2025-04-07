package com.github.xvandexen.helixkeys.functionaltity

import com.github.xvandexen.helixkeys.commands.CommandExecutor
import com.github.xvandexen.helixkeys.commands.SubMenu
import com.github.xvandexen.helixkeys.configuration.KeybindingConfig
import com.intellij.openapi.project.Project
typealias KeyBindingMap = Map<String, CommandExecutor.HelixCommand?>

class ModalKeyManager(
  private val modeManager: ModeManager,
  private val project: Project,
  private val keybindings: Map<String, KeybindingConfig.RecKeyBinding>) {

  private val normalModeBindings: KeyBindingMap = mutableMapOf()
  private val insertModeBindings: KeyBindingMap = mutableMapOf()
  private val visualModeBindings: KeyBindingMap = mutableMapOf()
  private val submenus: Map<String, SubMenu> = mutableMapOf()
  init {

    initBindings()
  }

  private fun initBindings() {
    keybindings.forEach{ topBinding ->
      when (topBinding.key) {
        "normal" -> parseBindings(topBinding.value,normalModeBindings)
        "insert" -> mutablemapof

      }
    }
  }

  fun parseBindings(unparsedBindings: KeybindingConfig.RecKeyBinding ,modeToBind: KeyBindingMap){
    when{
      unparsedBindings.command !== null && unpa->
    }



  }
}