package com.github.xvandexen.helixkeys.services.configuration

import com.intellij.openapi.diagnostic.thisLogger
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Class responsible for determining the appropriate file path for configuration files
 * based on the operating system.
 * 
 * This class follows OS-specific conventions for configuration file locations,
 * supporting Windows, macOS, and Unix-like systems.
 *
 * @property configFilename The name of the configuration file
 * @property pluginConfigDir The directory name for the plugin's configuration
 */
class ConfigPaths(
  private val configFilename: String,
  private val pluginConfigDir: String
) {
  /**
   * Logger instance for this class.
   */
  private val logger = thisLogger()

  /**
   * Gets the appropriate configuration file path based on the current operating system.
   * 
   * This method detects the operating system and delegates to the appropriate
   * OS-specific method to get the correct path.
   *
   * @return The Path object representing the configuration file location
   * @throws UnsupportedOperationException if the operating system is not recognized
   */
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

  /**
   * Gets the configuration file path for Windows systems.
   * 
   * This method follows Windows conventions for application data storage,
   * using the APPDATA environment variable if available, or falling back to
   * the user's home directory.
   *
   * @return The Path object representing the Windows configuration file location
   */
  private fun getWindowsPath(): Path {
    val appData = System.getenv("APPDATA")
    return if (appData != null) {
      Paths.get(appData, pluginConfigDir, configFilename)
    } else {
      Paths.get(System.getProperty("user.home"), "AppData", "Roaming", pluginConfigDir, configFilename)
    }
  }

  /**
   * Gets the configuration file path for macOS systems.
   * 
   * This method follows macOS conventions for application support files,
   * storing configuration in the Library/Application Support directory.
   *
   * @return The Path object representing the macOS configuration file location
   */
  private fun getMacPath(): Path = Paths.get(
    System.getProperty("user.home"),
    "Library/Application Support",
    pluginConfigDir,
    configFilename
  )

  /**
   * Gets the configuration file path for Unix-like systems.
   * 
   * This method follows the XDG Base Directory Specification for Unix-like systems,
   * using the XDG_CONFIG_HOME environment variable if available, or falling back to
   * the ~/.config directory.
   *
   * @return The Path object representing the Unix configuration file location
   */
  private fun getUnixPath(): Path {
    val xdgConfigHome = System.getenv("XDG_CONFIG_HOME")
    return if (xdgConfigHome != null) {
      Paths.get(xdgConfigHome, pluginConfigDir.lowercase(), configFilename)
    } else {
      Paths.get(System.getProperty("user.home"), ".config", pluginConfigDir.lowercase(), configFilename)
    }
  }
}
