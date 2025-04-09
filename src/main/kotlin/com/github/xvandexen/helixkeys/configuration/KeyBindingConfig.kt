package com.github.xvandexen.helixkeys.configuration

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import com.intellij.openapi.diagnostic.thisLogger
import java.awt.event.KeyEvent
import java.io.File
import java.nio.file.Path
import com.github.xvandexen.helixkeys.commands.CommandExecutor.HelixCommand
import com.github.xvandexen.helixkeys.functionaltity.ModeManager

class KeyBindingConfig() {

  data class RecKeyBinding(
    val command: HelixCommand? = null,
    val subBindings: Map<Set<Int>, RecKeyBinding>? = null
  )
  private val logger = thisLogger()


  private val specialKeyNames = mapOf(
    "escape" to KeyEvent.VK_ESCAPE,
    "enter" to KeyEvent.VK_ENTER,
    "space" to KeyEvent.VK_SPACE,
    "tab" to KeyEvent.VK_TAB,
    "backspace" to KeyEvent.VK_BACK_SPACE,
    "delete" to KeyEvent.VK_DELETE,
    "up" to KeyEvent.VK_UP,
    "down" to KeyEvent.VK_DOWN,
    "left" to KeyEvent.VK_LEFT,
    "right" to KeyEvent.VK_RIGHT
  )





  fun loadConfig(): MutableMap<ModeManager.Mode, Map<Set<Int>, RecKeyBinding>> {
    val configPath: Path = ConfigPaths("helixkeys_keybinding.toml", "Helixkeys").getOsSpecificConfigPath()
    val file = File(configPath.toUri())
    if (!file.exists()){
      file.parentFile?.mkdirs()
      file.createNewFile()
      logger.info("File created at: ${file.absolutePath}")
    } else logger.info("File Already Exists")



    val mapper = tomlMapper {



    }


    val tomlFile = configPath
    val config: Map<String, Map<String, Any>> = try{
     mapper.decode<Map<String, Map<String, Any>>>(tomlFile)
    }catch(e: Exception){
      //TODO(Add Popup about Config Parsing error)
      return DefaultConfig.createDefaultConfig()


    }
    logger.info("Config = : ${config}")
    val  parsedBindings = parseKeyBindings(config)
    logger.info("Parsed Config = $parsedBindings")
    return parsedBindings
  }

  private fun getKeyCodeFromString(key: String): Set<Int> {
    // This will be implemented by you separately

    return key.toSet().map { c -> KeyEvent.getExtendedKeyCodeForChar(c.code)}.toSet()
  }


  fun parseKeyBindings(map: Map<String, Any>): MutableMap<ModeManager.Mode, Map<Set<Int>, RecKeyBinding>> {
    // This function parses the top-level mode keys (normal, insert, etc.)
    // and returns a map of mode enum -> submappings
    val result = mutableMapOf<ModeManager.Mode, Map<Set<Int>, RecKeyBinding>>()

    for ((modeKey, value) in map) {
      // Convert the string mode to enum
      try {
        val modeEnum = ModeManager.Mode.valueOf(modeKey.uppercase())

        if (value is Map<*, *>) {
          @Suppress("UNCHECKED_CAST")
          val modeMap = value as Map<String, Any>
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

  private fun parseBindingsForMode(map: Map<String, Any>): MutableMap<Set<Int>, RecKeyBinding> {
    // This function handles the key bindings within a mode
    val result = mutableMapOf<Set<Int>, RecKeyBinding>()

    for ((key, value) in map) {
      val keyCode =  getKeyCodeFromString(key)

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

