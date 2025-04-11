package com.github.xvandexen.helixkeys.services.configuration

import ai.grazie.utils.isUppercase
import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import com.intellij.openapi.diagnostic.thisLogger
import java.awt.event.KeyEvent
import java.io.File
import java.nio.file.Path
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor.HelixCommand
import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager
import com.jetbrains.rd.generator.nova.PredefinedType

// Updated KeyCombo to use a character code, rather than Set<Int>
typealias KeyCombo = Pair<Char, KeyBindingConfig.Modifiers>

class KeyBindingConfig() {

  data class Modifiers(var control: Boolean = false, var meta: Boolean = false)

  // Updated RecKeyBinding to use KeyCombo instead of Set<Int>
  data class RecKeyBinding(
    val command: HelixCommand? = null,
    val subBindings: Map<Char, RecKeyBinding>? = null,
    val modifiers: Modifiers? = null
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
      logger.warn("Failed to parse Helix config file", e)
      mutableMapOf()
    }
  }

  // Updated to return KeyCombo (Char, Modifiers)
  private fun getKeyCodeFromString(keys: String): KeyCombo {
    val modifiers = Modifiers()

    // Process modifiers
    val keyWithoutModifiers = keys.replace("C-", "").also {
      if (it.length != keys.length) modifiers.control = true
    }.replace("M-", "").also {
      if (it.length != keys.length - (if (modifiers.control) 2 else 0)) modifiers.meta = true
    }

    // Get the main key character
    val keyChar = when {
      keyWithoutModifiers.length == 1 -> keyWithoutModifiers[0]
      specialKeyNames.containsKey(keyWithoutModifiers.lowercase()) ->
        specialKeyNames[keyWithoutModifiers.lowercase()] ?: '\u0000'
      else -> {
        logger.warn("Unknown key: $keyWithoutModifiers")
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
  private fun parseNestedBindings(map: Map<String, Any>): Map<Char, RecKeyBinding> {
    val result = mutableMapOf<Char, RecKeyBinding>()

    map.forEach { (keyStr, binding) ->
      val keyCombo = getKeyCodeFromString(keyStr)
      val keyChar = keyCombo.first
      val modifiers = keyCombo.second

      when (binding) {
        is String -> {
          result[keyChar] = RecKeyBinding(
            command = HelixCommand.valueOf(binding.uppercase()),
            modifiers = modifiers
          )
        }
        is Map<*, *> -> {
          @Suppress("UNCHECKED_CAST")
          val subBindings = parseNestedBindings(binding as Map<String, Any>)
          result[keyChar] = RecKeyBinding(
            subBindings = subBindings,
            modifiers = modifiers
          )
        }
      }
    }

    return result
  }
}