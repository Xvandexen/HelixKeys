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
typealias KeyCombo = Pair<Set<Char>, KeyBindingConfig.Modifiers>
class KeyBindingConfig() {

  data class Modifiers(var control: Boolean = false, var meta : Boolean = false)

  data class RecKeyBinding(
    val command: HelixCommand? = null,
    val subBindings: Map<Set<Int>, RecKeyBinding>? = null
  )
  private val logger = thisLogger()


  private val specialKeyNames = mapOf(
    "escape" to Char(27),
    "enter" to Char(10),
    "space" to ' ',
    "tab" to Char(9),
    "backspace" to Char(8),
    "delete" to Char(7),

  )





  fun loadConfig(): MutableMap<ModeManager.Mode, Map<KeyCombo, RecKeyBinding>> {
    val configPath: Path = ConfigPaths("helixkeys_keybinding.toml", "Helixkeys").getOsSpecificConfigPath()
    KeyEvent.VK_UP
    val file = File(configPath.toUri())
    if (!file.exists()){
      file.parentFile?.mkdirs()
      file.createNewFile()
      logger.info("File created at: ${file.absolutePath}")
    } else {
      logger.info("File Already Exists")
    }



    val mapper = tomlMapper {}


    val tomlFile = configPath
    val config: Map<String, Map<String, Any>> = try{
     mapper.decode<Map<String, Map<String, Any>>>(tomlFile)
    }catch(e: Exception){
      TODO("Add Popup about Config Parsing error")
      //return DefaultConfig.createDefaultConfig()


    }
    logger.info("Config = : ${config}")
    val  parsedBindings = parseKeyBindings(config)
    logger.info("Parsed Config = $parsedBindings")
    return parsedBindings
  }

  private fun getKeyCodeFromString(keys: String): KeyCombo {
    //TODO(Set Up Proper Parsing)
    var keySet: Set<Char> = mutableSetOf()
    var modifiers = Modifiers()
      val tokenRegex = Regex(
        // Tokens listed in order: modifiers (like C-), keywords (like escape)
        // and finally a single letter (matches any A-Z or a-z).
        """(C-|A-|S-|escape|enter|space|tab|backspace|delete|up|down|left|right|[a-zA-Z])"""
      )
      val tokens =
        tokenRegex
        .findAll(keys)
        .map{it.value}
        .toList()

      logger.info("Split From Config: $tokens")
    var nextToUpper = false
        tokens.forEach { keycode ->
          var keycode = keycode
          if (nextToUpper) {keycode = keycode.uppercase(); nextToUpper = false}

        when{
          keycode == "C-" -> modifiers.control= true
          keycode == "A-" -> modifiers.meta = true
          //Shift converts alpha upper
          keycode == "S-" ->  nextToUpper = true

          keycode in setOf("escape", "enter", "space", "tab", "backspace", "delete", "up", "down" , "left" , "right") -> specialKeyNames[keycode]?.let{it.se}
          keycode.isUppercase() && keycode.length == 1 ->  addAll(listOf(KeyEvent.VK_SHIFT, KeyEvent.getExtendedKeyCodeForChar(keycode[0].code)))
          keycode.length == 1 -> add(KeyEvent.getExtendedKeyCodeForChar(keycode[0].code))
            }


          }
        }
      }








  fun parseKeyBindings(map: Map<String, Any>): MutableMap<ModeManager.Mode, Map<KeyCombo, RecKeyBinding>> {
    // This function parses the top-level mode keys (normal, insert, etc.)
    // and returns a map of mode enum -> submappings
    val result = mutableMapOf<ModeManager.Mode, Map<Set<Int>, RecKeyBinding>>()

    for ((modeKey, value) in map) {
      // Convert the string mode to enum
      try {
        val modeEnum = ModeManager.Mode.valueOf(modeKey.uppercase())

        if (value is Map<*, *>) {
          @Suppress("UNCHECKED_CAST")
          val modeMap = value as Map<KeyCombo, Any>
          result[modeEnum] = parseBindingsForMode(modeMap)
        }
      } catch (e: IllegalArgumentException) {
        // Skip modes that don't match our enum
        // Or log them if needed
        println("Warning: Unrecognized mode: $modeKey")
      }
    }

    return result
  }

  private fun parseBindingsForMode(map: Map<String, Any>): MutableMap<KeyCombo, RecKeyBinding> {
    // This function handles the key bindings within a mode
    val result = mutableMapOf<Set<Int>, RecKeyBinding>()

    for ((key, value) in map) {
      val keyCombo: KeyCombo =  getKeyCodeFromString(key)

      when (value) {
        is String -> {
          // Convert the string command to enum
          try {
            val commandEnum = HelixCommand.valueOf(value.uppercase())
            result[keyCode] = RecKeyBinding(command = commandEnum)
          } catch (e: IllegalArgumentException) {
            // Skip commands that don't match our enum
            // Or log them if needed
            println("Warning: Unrecognized command: $value")
          }
        }
        is Map<*, *> -> {
          @Suppress("UNCHECKED_CAST")
          val nestedMap = value as Map<String, Any>
          val subBindings = parseBindingsForMode(nestedMap)
          result[keyCode] = RecKeyBinding(subBindings = subBindings)
        }
      }
    }

    return result
  }




}

