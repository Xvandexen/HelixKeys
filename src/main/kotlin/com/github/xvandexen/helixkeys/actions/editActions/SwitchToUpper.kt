package com.github.xvandexen.helixkeys.actions.editActions

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager

/**
 * Action that converts the selected text to uppercase.
 * 
 * This action implements the Helix editor's "~" command when used with uppercase.
 * If no text is selected, it converts the character at the current cursor position.
 */
class SwitchToUpper: AnAction() {

  /**
   * Performs the action of converting text to uppercase.
   * 
   * If there's no selection, it selects the character at the current cursor position.
   * Then it converts all selected characters to uppercase and replaces the original text.
   *
   * @param e The action event containing context information
   */
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

    val uppercasedSelection: CharSequence = selectionModel
      .selectedText
      ?.map { it.uppercaseChar() }
      ?.joinToString("")
      .orEmpty()

    WriteCommandAction.runWriteCommandAction(executor.project) {
      document.replaceString(start, end, uppercasedSelection)
    }
  }
}
