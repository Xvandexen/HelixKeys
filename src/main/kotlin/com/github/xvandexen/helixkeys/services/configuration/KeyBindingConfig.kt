package com.github.xvandexen.helixkeys.services.configuration

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import com.intellij.openapi.diagnostic.thisLogger
import java.nio.file.Path
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor.HelixCommand
import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager
import com.github.xvandexen.helixkeys.services.utilities.NotificationErrorHandling

// Updated KeyCombo to use a character code, rather than Set<Int>
typealias KeyCombo = Pair<Char, KeyBindingConfig.Modifiers>

object KeyBindingConfig {

  data class Modifiers(var control: Boolean = false, var meta: Boolean = false, var alt: Boolean = false)

  // Updated RecKeyBinding to use KeyCombo instead of Set<Int>
  data class RecKeyBinding(
    val command: HelixCommand? = null,
    val subBindings: Map<KeyCombo, RecKeyBinding>? = null,
  )

  private val logger = thisLogger()

  // Updated to map strings to characters
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

      // Return empty map or default configuration
      return mutableMapOf()

    }
  }


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

  // Helper method to parse nested bindings
  // Updated to use KeyCombo instead of Char
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