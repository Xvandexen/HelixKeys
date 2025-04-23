package com.github.xvandexen.helixkeys.services.configuration

import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager

/**
 * Object responsible for providing default key binding configurations.
 * 
 * This object serves as a factory for creating default key binding configurations
 * when no user-defined configuration is available.
 */
object DefaultConfig {
  /**
   * Creates a default configuration for key bindings.
   * 
   * Currently returns an empty map, suggesting that default configurations
   * may be defined elsewhere or that this is a placeholder for future implementation.
   *
   * @return A mutable map associating editor modes with key binding configurations
   */
  fun createDefaultConfig(): MutableMap<ModeManager.Mode, Map<Set<Int>, KeyBindingConfig.RecKeyBinding>>{
    return mutableMapOf()
  }
}
