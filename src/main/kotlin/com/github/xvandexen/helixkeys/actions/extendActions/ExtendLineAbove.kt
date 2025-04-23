package com.github.xvandexen.helixkeys.actions.extendActions

import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType

class ExtendLineAbove : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR) ?: return
    val document = editor.document
    val selectionModel = editor.selectionModel
    val caretModel = editor.caretModel

    val currentLine = caretModel.logicalPosition.line
    if (currentLine > 0) {
      val startLine = if (selectionModel.hasSelection()) {
        selectionModel.selectionStartPosition?.line ?: currentLine
      } else {
        currentLine
      }

      val targetLine = startLine - 1
      val endOffset = document.getLineEndOffset(currentLine)
      val startOffset = document.getLineStartOffset(targetLine)

      selectionModel.setSelection(startOffset, endOffset)
      caretModel.moveToOffset(endOffset)
    }
  }
}