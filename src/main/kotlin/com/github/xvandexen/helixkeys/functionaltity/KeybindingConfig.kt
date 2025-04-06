package com.github.xvandexen.helixkeys.functionaltity

import cc.ekblad.toml.decode
import cc.ekblad.toml.model.TomlValue
import cc.ekblad.toml.tomlMapper
import com.intellij.openapi.diagnostic.thisLogger
import java.awt.event.KeyEvent
import java.io.File
import java.nio.file.Path

class KeybindingConfig() {
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

  data class KeyCombination(val keyCodes: Set<Int>)




  fun loadConfig(): Map<String, RecKeyBinding> {
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
    val config = mapper.decode<Map<String, Map<String, Any>>>(tomlFile)
    logger.info("Config = : ${config}")
    val  parsedBindings = parseKeyBindings(config)
    logger.info("Parsed Config = $parsedBindings")
    return parsedBindings
  }

  data class RecKeyBinding(
    val key: String,
    val command: String? = null,
    val subBindings: Map<String, RecKeyBinding>? = null
  )
  fun parseKeyBindings(map: Map<String, Any>, parentKey: String = ""): Map<String, RecKeyBinding> {
    //TODO(Comment this better )
    val result = mutableMapOf<String, RecKeyBinding>()

    for ((key, value) in map) {
      when (value) {
        is String -> {

          result[key] = RecKeyBinding(key = key, command = value)
        }
        is Map<*, *> -> {
          @Suppress("UNCHECKED_CAST")
          val nestedMap = value as Map<String, Any>
          val subBindings = parseKeyBindings(nestedMap, key)
          result[key] = RecKeyBinding(key = key, subBindings = subBindings)
        }
      }
    }

    return result
  }





  }

