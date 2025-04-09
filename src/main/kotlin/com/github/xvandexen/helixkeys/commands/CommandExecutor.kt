package com.github.xvandexen.helixkeys.commands

import com.github.xvandexen.helixkeys.functionaltity.ModeManager
import com.github.xvandexen.helixkeys.functionaltity.ModeManager.Mode.*
import com.intellij.openapi.diagnostic.thisLogger

object CommandExecutor {

  val lastCommand: HelixCommand? = null

  enum class HelixCommand(val commandFunction: ( Any?)-> Boolean ) {
    ENTER_NORMAL_MODE({
      ModeManager.toggleMode(NORMAL)
      thisLogger().info("Executing Command: Normal Mode")
      true
    }),
    ENTER_INSERT_MODE({
      ModeManager.toggleMode(INSERT)
      thisLogger().info("Executing Command: InsertMode")
      true
    }),

    MOVE_CHAR_LEFT({
      thisLogger().info("Executing Command: MoveCharLeft")

      true}),
    MOVE_VISUAL_LINE_UP({true}),
    MOVE_VISUAL_LINE_DOWN({true}),
    MOVE_CHAR_RIGHT({true}),;
  }


  fun executeCommand(command: HelixCommand, args: Any? = null ){
    thisLogger().info("Attempting to Execute: ${command}")

    command.commandFunction.invoke(args)

}
   fun exampleCommand(): Boolean {
    thisLogger().info("Command Run")
    return true
  }




  }
