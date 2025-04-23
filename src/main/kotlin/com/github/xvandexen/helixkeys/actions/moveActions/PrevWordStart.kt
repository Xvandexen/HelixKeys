package com.github.xvandexen.helixkeys.actions.moveActions

import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.getEditorComponents
import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.isSameCharType
import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.isWhitespace
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action that moves the caret to the start of the previous word.
 * 
 * This action implements the Helix editor's "b" command, which moves the cursor
 * to the beginning of the previous word. It also selects the text between the current
 * position and the previous word start.
 */
class PrevWordStart : AnAction(){
  /**
   * Performs the action of moving to the previous word start.
   * 
   * The method starts from the character before the current position, identifies its type,
   * then moves backward through characters of the same type to find the start of the previous word.
   *
   * @param e The action event containing context information
   */
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
