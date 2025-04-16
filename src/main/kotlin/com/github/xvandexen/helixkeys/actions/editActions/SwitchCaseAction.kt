package com.github.xvandexen.helixkeys.actions.editActions

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.fileEditor.FileEditorManager

class SwitchCaseAction : AnAction(){
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project
    val executor = CommandExecutor.getInstance(project!!)
    val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
    val document = editor.document
    val selectionModel = editor.selectionModel



    if (!selectionModel.hasSelection()) {
      selectionModel.setSelection(
        editor.caretModel.offset,
        editor.caretModel.offset + 1
      )
    }
    val start = selectionModel.selectionStart
    val end = selectionModel.selectionEnd


    val switchedSelection: CharSequence = selectionModel
      .selectedText
      ?.map {
        when(it) {
          in 'a'..'z' -> it.uppercaseChar()
          in 'A'..'Z' -> it.lowercaseChar()
          else -> it
        }
      }?.joinToString("").toString()

    WriteCommandAction.runWriteCommandAction(executor.project) {
      document.replaceString(start,end, switchedSelection)


    }
  }


}