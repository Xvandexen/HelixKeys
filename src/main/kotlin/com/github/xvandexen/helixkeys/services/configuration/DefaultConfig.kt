package com.github.xvandexen.helixkeys.services.configuration

import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager

object DefaultConfig {
  fun createDefaultConfig(): MutableMap<ModeManager.Mode, Map<Set<Int>, KeyBindingConfig.RecKeyBinding>>{
    return mutableMapOf()
  }
}