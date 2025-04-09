package com.github.xvandexen.helixkeys.commands

import com.intellij.openapi.diagnostic.thisLogger

object CommandExecutor {

  val lastCommand: HelixCommand? = null

  enum class HelixCommand(val commandFunction: ( Any?)-> Boolean ) {
    ENTER_NORMAL_MODE({
      thisLogger().info("Executing Command: Normal Mode")
      true
    }),

    MOVE_CHAR_LEFT({true}),
    MOVE_VISUAL_LINE_UP({true}),
    MOVE_VISUAL_LINE_DOWN({true}),
    MOVE_CHAR_DOWN({true}),;
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
