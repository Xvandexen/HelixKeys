package com.github.xvandexen.helixkeys.actions.extendActions

import com.github.xvandexen.helixkeys.services.Registry
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.ScrollType

class Extend_Line : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val editor = e.getData(CommonDataKeys.EDITOR) ?: return
    val project = e.project ?: return
    val document = editor.document
    val selectionModel = editor.selectionModel
    val caretModel = editor.caretModel

    val registry = Registry.getInstance(project)
    val editorRegister = registry.getRegister(editor)?: run {
      registry.addRegistry(editor)
      registry.getRegister(editor)
    }!!

    if (!selectionModel.hasSelection()) {
      // Select current line if nothing is selected
      val currentLineNumber = caretModel.logicalPosition.line
      val lineStartOffset = document.getLineStartOffset(currentLineNumber)
      val lineEndOffset = document.getLineEndOffset(currentLineNumber)

      selectionModel.setSelection(lineStartOffset, lineEndOffset)

      // Set anchor at the start and caret at the end
      editorRegister.updateAnchor(lineStartOffset)
      caretModel.moveToOffset(lineEndOffset)

      // Add to selection history
      editorRegister?.addSelectionToHistory(lineStartOffset, lineEndOffset)
    } else {
      // Get the anchor position
      val anchor = editorRegister.anchor ?: run {
        // If no anchor is set yet, determine it based on caret position
        val newAnchor = if (caretModel.offset == selectionModel.selectionEnd)
          selectionModel.selectionStart else selectionModel.selectionEnd
        editorRegister.updateAnchor(newAnchor)
        newAnchor
      }

      // Check if anchor is at start or end of selection
      val isAnchorAtSelectionStart = anchor == selectionModel.selectionStart

      if (isAnchorAtSelectionStart) {
        // Anchor is at selection start, extend downward
        val currentEndLine = editor.offsetToLogicalPosition(selectionModel.selectionEnd).line
        if (currentEndLine < document.lineCount - 1) {
          val newEndOffset = document.getLineEndOffset(currentEndLine + 1)
          selectionModel.setSelection(anchor, newEndOffset)
          caretModel.moveToOffset(newEndOffset)
          editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)

          // Add to selection history
          editorRegister.addSelectionToHistory(anchor, newEndOffset)
        }
      } else {
        // Anchor is at selection end, extend upward
        val currentStartLine = editor.offsetToLogicalPosition(selectionModel.selectionStart).line
        if (currentStartLine > 0) {
          val newStartOffset = document.getLineStartOffset(currentStartLine - 1)
          selectionModel.setSelection(newStartOffset, anchor)
          caretModel.moveToOffset(newStartOffset)
          editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)

          // Add to selection history
          editorRegister.addSelectionToHistory(newStartOffset, anchor)
        }
      }
    }
  }

  override fun update(e: AnActionEvent) {
    val editor = e.getData(CommonDataKeys.EDITOR)
    e.presentation.isEnabled = editor != null
  }
}