package com.github.xvandexen.helixkeys.actions.moveActions

import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.getEditorComponents
import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.isWhitespace
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class LongNextWordStart :AnAction() {
  override fun actionPerformed(e: AnActionEvent) {

      val components = getEditorComponents(e.project!!)?: return
      val (caretModel, _, selectionModel, text) = components

      val currentPosition = caretModel.offset

      var newOffset = currentPosition
      if (newOffset < text.length && !isWhitespace(text[newOffset])) {
        while (newOffset < text.length && !isWhitespace(text[newOffset])) {
          newOffset++
        }
      }
      while (newOffset < text.length && isWhitespace(text[newOffset])) {
        newOffset++
      }

      if (newOffset != currentPosition) {
        selectionModel.setSelection(currentPosition, newOffset)
        caretModel.moveToOffset(newOffset)
      }
    }

  }
