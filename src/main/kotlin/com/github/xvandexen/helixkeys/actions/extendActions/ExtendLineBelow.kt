package com.github.xvandexen.helixkeys.actions.extendActions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.LogicalPosition

class ExtendLineBelow() : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val editor = e.getData(CommonDataKeys.EDITOR) ?: return
    val caretModel = editor.caretModel
    val selectionModel = editor.selectionModel
    val document = editor.document

    val currentLine = caretModel.logicalPosition.line
    val nextLine = (currentLine + 1).coerceAtMost(document.lineCount - 1)

    if (!selectionModel.hasSelection()) {
      val lineStartOffset = document.getLineStartOffset(currentLine)
      val lineEndOffset = document.getLineEndOffset(currentLine)
      selectionModel.setSelection(lineStartOffset, lineEndOffset)
    } else {
      val selectionEnd = selectionModel.selectionEnd
      val nextLineEndOffset = document.getLineEndOffset(nextLine)
      selectionModel.setSelection(selectionModel.selectionStart, nextLineEndOffset)
    }
  }
}