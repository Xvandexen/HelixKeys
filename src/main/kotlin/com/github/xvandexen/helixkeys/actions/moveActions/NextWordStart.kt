package com.github.xvandexen.helixkeys.actions.moveActions

import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.getEditorComponents
import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.isSameCharType
import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.isWhitespace
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class NextWordStart : AnAction() {


  override fun actionPerformed(e: AnActionEvent) {

    val components = getEditorComponents(e.project!!)?: return
    val (caretModel, _, selectionModel, text) = components
    val currentPosition = caretModel.offset

    if (currentPosition >= text.length) return // End of document

    val startChar = text[currentPosition]
    val isAlpha = Character.isLetter(startChar)
    val isWhitespace = isWhitespace(startChar)

    var nextPosition = currentPosition
    while (nextPosition < text.length && isSameCharType(text[nextPosition], isAlpha, isWhitespace)) {
      nextPosition++
    }
    while (nextPosition < text.length && isWhitespace(text[nextPosition])) {
      nextPosition++
    }

    if (nextPosition > currentPosition) {
      selectionModel.setSelection(currentPosition, nextPosition)
      caretModel.moveToOffset(nextPosition)
    }

  }


}