package com.github.xvandexen.helixkeys.services.configuration

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import com.intellij.openapi.diagnostic.thisLogger
import java.nio.file.Path
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor.HelixCommand
import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager
import com.github.xvandexen.helixkeys.services.utilities.NotificationErrorHandling

/**
 * Represents a key combination consisting of a character and its modifiers.
 * 
 * This typealias pairs a character (the key) with modifiers (Ctrl, Alt, Meta)
 * to represent keyboard shortcuts in the Helix-style key binding system.
 */
typealias KeyCombo = Pair<Char, KeyBindingConfig.Modifiers>

/**
 * Object responsible for loading and parsing key binding configurations.
 * 
 * This object handles loading key binding configurations from TOML files,
 * parsing them into a structured format, and providing access to the key bindings
 * for different editor modes.
 */
object KeyBindingConfig {

  /**
   * Data class representing key modifiers.
   * 
   * This class tracks which modifier keys (Control, Meta/Command, Alt) are active
   * for a key combination.
   *
   * @property control Whether the Control key is active
   * @property meta Whether the Meta/Command key is active
   * @property alt Whether the Alt key is active
   */
  data class Modifiers(var control: Boolean = false, var meta: Boolean = false, var alt: Boolean = false)

  /**
   * Data class representing a recursive key binding.
   * 
   * This class can represent either a direct command binding or a nested set of bindings
   * that form a key sequence (e.g., "g" followed by "g" to go to the beginning of the file).
   *
   * @property command The command to execute when this binding is activated, or null if this is a parent binding
   * @property subBindings Map of sub-bindings for key sequences, or null if this is a leaf binding
   */
  data class RecKeyBinding(
    val command: HelixCommand? = null,
    val subBindings: Map<KeyCombo, RecKeyBinding>? = null,
  )

  /**
   * Logger instance for this class.
   */
  private val logger = thisLogger()

  /**
   * Map of special key names to their character representations.
   * 
   * This map associates human-readable key names (like "escape", "enter", "space")
   * with their character representations. For standard keys, this uses the actual
   * character codes. For special keys like arrows, it uses custom Unicode code points.
   */
  private val specialKeyNames = mapOf(
      "escape" to '\u001B',
      "enter" to '\n',
      "space" to ' ',
      "tab" to '\t',
      "backspace" to '\b',
      "delete" to '\u007F',
      // Using custom char codes for arrow keys and navigation keys
      "up" to '\uE000',
      "down" to '\uE001',
      "left" to '\uE002',
      "right" to '\uE003',
      "end" to '\uE004',
      "home" to '\uE005'
    )

  /**
   * Formats a KeyCombo as a human-readable string.
   * 
   * This extension function converts a key combination to a string representation
   * with modifiers prefixed (e.g., "C-A-x" for Ctrl+Alt+x).
   *
   * @return A string representation of the key combination
   */
  fun KeyCombo.toFormattedString(): String {
    val (char, modifiers) = this
    val prefix = StringBuilder()

    if (modifiers.control) prefix.append("C-")
    if (modifiers.meta) prefix.append("M-")
    if (modifiers.alt) prefix.append("A-")

    return prefix.toString() + char
  }

  /**
   * Loads key binding configuration from a TOML file.
   * 
   * This method attempts to load the key binding configuration from a TOML file
   * located at the OS-specific configuration path. If the file doesn't exist or
   * can't be parsed, it returns an empty map and logs an error.
   *
   * @return A mutable map associating editor modes with their key bindings
   */
  fun loadConfig(): MutableMap<ModeManager.Mode, Map<KeyCombo, RecKeyBinding>> {
    val mapper = tomlMapper {}
    val configFile = ConfigPaths("helixkeys_keybinding.toml", "helixkeys").getOsSpecificConfigPath().toFile()

    if(!configFile.exists()) {
      logger.warn("Helix config file does not exist, using defaults")
      return mutableMapOf()
    }

    return try {
      val configData: Map<String, Map<String,Map<String,Any>>> = mapper.decode(Path.of(configFile.absolutePath))
      logger.info("Unparsed Bindings = $configData")
      val parsedBindings = parseKeyBindings(configData)
      logger.info("ParsedBindings = $parsedBindings")
       parsedBindings
    } catch (e: Exception) {
      val errorMessage = e.message ?: "Unknown error parsing config file"
      NotificationErrorHandling.showErrorNotification(
        null,
        "HelixKeys: Configuration Error",
        errorMessage
      )
      // Still log for debugging purposes
      logger.error("Failed to parse config file", e)

      // Return emptymap or default configuration
      return mutableMapOf()
    }
  }

  /**
   * Converts a string representation of a key to a KeyCombo object.
   * 
   * This method parses a string like "C-A-x" (Ctrl+Alt+x) into a KeyCombo object
   * with the appropriate character and modifiers. It handles special key names
   * and control characters.
   *
   * @param keys The string representation of the key combination
   * @return A KeyCombo object representing the key combination
   */
  private fun getKeyCodeFromString(keys: String): KeyCombo {
    val modifiers = Modifiers()

    modifiers.control = keys.contains("C-")
    modifiers.meta = keys.contains("M-")
    modifiers.alt = keys.contains("A-")
    val cleanKey: String = keys
        .replace("C-", "")
        .replace("M-", "")
        .replace("A-", "")

    // Get the main key character
    val keyChar: Char = when {
      cleanKey.length == 1 && modifiers.control-> (cleanKey[0].lowercaseChar() - 96)
      cleanKey.length == 1 -> cleanKey[0]
      specialKeyNames.containsKey(cleanKey) ->
        specialKeyNames[cleanKey] ?: '\u0000'

      else -> {
        logger.warn("Unknown key: $cleanKey")
        '\u0000' // Null character for unknown keys
      }
    }

    return KeyCombo(keyChar, modifiers)
  }


  /**
   * Parses key bindings from the TOML configuration structure.
   * 
   * This method processes the top-level structure of the TOML configuration,
   * handling different editor modes (normal, insert, select) and creating
   * a map of key combinations to their corresponding commands or sub-bindings.
   *
   * @param map The parsed TOML configuration as a nested map structure
   * @return A mutable map associating editor modes with their key bindings
   */
  private fun parseKeyBindings(map: Map<String, Map<String, Map<String, Any>>>): MutableMap<ModeManager.Mode, Map<KeyCombo, RecKeyBinding>> {
    val result = mutableMapOf<ModeManager.Mode, Map<KeyCombo, RecKeyBinding>>()

    map.forEach { (category, modeBindings) ->
      if (category == "keys") {
        // Process normal mode bindings
        val normalBindings = mutableMapOf<KeyCombo, RecKeyBinding>()

        modeBindings.forEach { (modeKey, bindings) ->
          when (modeKey) {
            "normal" -> {
              bindings.forEach { (keyString, value) ->
                val keyCombo = getKeyCodeFromString(keyString)

                when (value) {
                  is String -> {
                    // Direct command binding
                    val command = try {
                      HelixCommand.valueOf(value.uppercase())
                    } catch (e: IllegalArgumentException) {
                      logger.warn("Unknown command: $value")
                      null
                    }
                    normalBindings[keyCombo] = RecKeyBinding(command = command)
                  }
                  is Map<*, *> -> {
                    // Handle nested bindings
                    @Suppress("UNCHECKED_CAST")
                    val subBindings = parseNestedBindings(value as Map<String, Any>)
                    normalBindings[keyCombo] = RecKeyBinding(subBindings = subBindings)
                  }
                }
              }
              result[ModeManager.Mode.NORMAL] = normalBindings
            }
            "insert" -> {
              // Process insert mode bindings
              val insertBindings = mutableMapOf<KeyCombo, RecKeyBinding>()

              bindings.forEach { (keyString, value) ->
                val keyCombo = getKeyCodeFromString(keyString)

                when (value) {
                  is String -> {
                    // Direct command binding
                    val command = try {
                      HelixCommand.valueOf(value.uppercase())
                    } catch (e: IllegalArgumentException) {
                      logger.warn("Unknown command: $value")
                      null
                    }
                    insertBindings[keyCombo] = RecKeyBinding(command = command)
                  }
                  is Map<*, *> -> {
                    // Handle nested bindings
                    @Suppress("UNCHECKED_CAST")
                    val subBindings = parseNestedBindings(value as Map<String, Any>)
                    insertBindings[keyCombo] = RecKeyBinding(subBindings = subBindings)
                  }
                }
              }
              result[ModeManager.Mode.INSERT] = insertBindings
            }
            "select" -> {
              // Process insert mode bindings
              val selectBindings = mutableMapOf<KeyCombo, RecKeyBinding>()

              bindings.forEach { (keyString, value) ->
                val keyCombo = getKeyCodeFromString(keyString)

                when (value) {
                  is String -> {
                    // Direct command binding
                    val command = try {
                      HelixCommand.valueOf(value.uppercase())
                    } catch (e: IllegalArgumentException) {
                      logger.warn("Unknown command: $value")
                      null
                    }
                    selectBindings[keyCombo] = RecKeyBinding(command = command)
                  }
                  is Map<*, *> -> {
                    // Handle nested bindings
                    @Suppress("UNCHECKED_CAST")
                    val subBindings = parseNestedBindings(value as Map<String, Any>)
                    selectBindings[keyCombo] = RecKeyBinding(subBindings = subBindings)
                  }
                }
              }
              result[ModeManager.Mode.SELECT] = selectBindings
            }
          }
        }
      }
    }

    // Log the results for debugging
    logger.debug("Parsed key bindings: ${result.size} modes")
    result.forEach { (mode, bindings) ->
      logger.debug("  Mode $mode: ${bindings.size} bindings")
    }

    return result
  }
    // Add more debug logging

  /**
   * Helper method to parse nested bindings for complex key sequences.
   * 
   * This method recursively processes nested key bindings, allowing for
   * complex key sequences like "g" followed by "g" to go to the beginning of the file.
   * It converts string keys to KeyCombo objects and handles both direct command bindings
   * and further nested bindings.
   *
   * @param map The map of key strings to either command strings or nested binding maps
   * @return A map of KeyCombo objects to their corresponding RecKeyBinding objects
   */
  private fun parseNestedBindings(map: Map<String, Any>): Map<KeyCombo, RecKeyBinding> {
    val result = mutableMapOf<KeyCombo, RecKeyBinding>()

    map.forEach { (key, value) ->
      // Parse key to get KeyCombo instead of just Char
      val keyCombo = getKeyCodeFromString(key)

      // Process the value based on whether it's a command or nested bindings
      when (value) {
        is String -> {
          // Handle command case
          val command = HelixCommand.valueOf(value.uppercase())
          result[keyCombo] = RecKeyBinding(command = command)
        }
        is Map<*, *> -> {
          // Handle nested bindings case
          @Suppress("UNCHECKED_CAST")
          val nestedMap = value as Map<String, Any>
          val nestedBindings = parseNestedBindings(nestedMap)
          result[keyCombo] = RecKeyBinding(subBindings = nestedBindings)
        }
      }
    }

    return result
  }
}
