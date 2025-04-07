package com.github.xvandexen.helixkeys.configuration

import com.intellij.openapi.diagnostic.thisLogger
import java.nio.file.Path
import java.nio.file.Paths

class ConfigPaths(
  private val  configFilename: String,
  private val pluginConfigDir: String

  ) {
  private val logger = thisLogger()


  public fun getOsSpecificConfigPath(): Path {
    val os = System.getProperty("os.name").lowercase()
    logger.info("Operating System is: $os")
    return when{
      os.contains(Regex("nix|nux|aix"))  -> getUnixPath()
      os.contains("mac") ->getMacPath()
      os.contains("win") -> getWindowsPath()
      else -> throw UnsupportedOperationException("Unsupported operating system: $os")
    }

  }

  private fun  getWindowsPath(): Path {
    val appData = System.getenv("APPDATA")
    return if (appData != null) {
      Paths.get(appData, pluginConfigDir, configFilename)
    } else {
      Paths.get(System.getProperty("user.home"), "AppData", "Roaming", pluginConfigDir, configFilename)
    }

  }

  private fun getMacPath(): Path = Paths.get(
    System.getProperty("user.home"),
    "Library/Application Support",
    pluginConfigDir,
    configFilename
  )

  private fun getUnixPath(): Path {
    val xdgConfigHome = System.getenv("XDG_CONFIG_HOME")
    return if (xdgConfigHome != null) {
      Paths.get(xdgConfigHome, pluginConfigDir.lowercase(), configFilename)
    } else {
      Paths.get(System.getProperty("user.home"), ".config", pluginConfigDir.lowercase(), configFilename)
    }
  }
}
