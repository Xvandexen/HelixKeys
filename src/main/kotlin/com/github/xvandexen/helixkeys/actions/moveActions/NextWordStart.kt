package com.github.xvandexen.helixkeys.actions.moveActions

import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.getEditorComponents
import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.isSameCharType
import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.isWhitespace
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action that moves the caret to the start of the next word.
 * 
 * This action implements the Helix editor's "w" command, which moves the cursor
 * to the beginning of the next word. It also selects the text between the current
 * position and the next word start.
 */
class NextWordStart : AnAction() {

  /**
   * Performs the action of moving to the next word start.
   * 
   * The method identifies the current character type (letter, whitespace, or other),
   * then moves past all characters of the same type, and finally past any whitespace
   * to reach the start of the next word.
   *
   * @param e The action event containing context information
   */
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
