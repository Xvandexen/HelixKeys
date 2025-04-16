package com.github.xvandexen.helixkeys.actions.moveActions

import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.getEditorComponents
import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.isSameCharType
import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.isWhitespace
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class PrevWordStart : AnAction(){
  override fun actionPerformed(e: AnActionEvent) {
    val components = getEditorComponents(e.project!!)?: return
    val (caretModel, _, selectionModel, text) = components
    val currentPosition = caretModel.offset - 1

    if (currentPosition < 0) return // Start of document

    val startChar = text[currentPosition]
    val isAlpha = Character.isLetter(startChar)
    val isWhitespace = isWhitespace(startChar)

    var previousPosition = currentPosition
    while (previousPosition >= 0 && isSameCharType(text[previousPosition], isAlpha, isWhitespace)) {
      previousPosition--
    }
    val wordStart = previousPosition + 1

    if (wordStart < caretModel.offset) {
      selectionModel.setSelection(wordStart, caretModel.offset)
      caretModel.moveToOffset(wordStart)
    }
  }

}