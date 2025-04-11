package com.github.xvandexen.helixkeys.services.commands

import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager
import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager.Mode.INSERT
import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager.Mode.NORMAL
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer

@Service(Service.Level.PROJECT)
class CommandExecutor(val project: Project): Disposable {

  private val modeManager = ModeManager.getInstance(project)

  private lateinit var context: DataContext
  val lastCommand: HelixCommand? = null
  lateinit var editor: EditorEx
  private set

  enum class HelixCommand(val commandFunction: (CommandExecutor, Any?)-> Any? ) {

    ENTER_NORMAL_MODE({x,_ -> x.modeManager.toggleMode(NORMAL) }),
    ENTER_INSERT_MODE({ x,_ -> x.modeManager.toggleMode(INSERT) }),

    MOVE_CHAR_LEFT({x, _ -> x.executeActionById("EditorLeft")}),
    MOVE_VISUAL_LINE_DOWN({ x,_ -> x.executeActionById("EditorDown")}),
    MOVE_VISUAL_LINE_UP({ x,_ -> x.executeActionById("EditorUp")}),
    MOVE_CHAR_RIGHT({ x,_ -> x.executeActionById("EditorRight") }),

    MOVE_NEXT_WORD_START({x,_ -> MoveActions.nextWordStart(x) }),
    MOVE_PREV_WORD_START({x,_ -> MoveActions.prevWordStart(x)}),

    MOVE_NEXT_WORD_END({x,_ -> MoveActions.nextWordEnd(x)}),
    MOVE_PREV_WORD_END({x,_ -> MoveActions.prevWordEnd(x)}),

    MOVE_NEXT_LONG_WORD_START({x,_->}),
    MOVE_PREV_LONG_WORD_START({x,_->}),
    MOVE_NEXT_LONG_WORD_END({x,_->})


  }

  fun executeCommand(command: HelixCommand, args: Any? = null) {

    editor = (FileEditorManager.getInstance(this.project).selectedTextEditor!!) as EditorEx
    context = editor.dataContext

    thisLogger().info("Attempting to Execute: $command")

    command.commandFunction.invoke(this,args)

  }


    fun executeActionById(actionId: String) {
    val action = ActionManager.getInstance().getAction(actionId)
      thisLogger().info("Action $action")
      thisLogger().info("Editor = $editor")
      thisLogger().info("Context = $context")
    val event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN, context)
    action.actionPerformed(event)
  }

  override fun dispose() {
    Disposer.dispose(this)
  }

  fun executeCommandXTimes(numTimes: String, command: HelixCommand, args: Any? = null) {
    editor = (FileEditorManager.getInstance(this.project).selectedTextEditor!!) as EditorEx
    context = editor.dataContext


    thisLogger().info("Attempting to Execute: $command x $numTimes")
    for (i in 1..numTimes.toInt()){

    command.commandFunction.invoke(this,args)
    }

  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): CommandExecutor= project.service()
  }





}
