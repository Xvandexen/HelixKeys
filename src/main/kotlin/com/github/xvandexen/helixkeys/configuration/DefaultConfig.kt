package com.github.xvandexen.helixkeys.configuration

import com.github.xvandexen.helixkeys.functionaltity.ModeManager

object DefaultConfig {
  fun createDefaultConfig(): MutableMap<ModeManager.Mode, Map<Set<Int>, KeyBindingConfig.RecKeyBinding>>{
    return mutableMapOf()
  }
}