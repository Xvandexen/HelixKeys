package com.github.xvandexen.helixkeys.actions.moveActions

import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.getEditorComponents
import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.isSameCharType
import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.isWhitespace
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class PrevWordEnd : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {


    val components = getEditorComponents(e.project!!)?: return
    val (caretModel, _, selectionModel, text) = components
    var currentPosition = caretModel.offset - 1

    if (currentPosition < 0) return // Start of document

    val startChar = if (currentPosition >= 0) text[currentPosition] else ' '
    val isAlpha = Character.isLetter(startChar)
    val isWhitespace = isWhitespace(startChar)

    // Move backward through characters of the same type
    while (currentPosition >= 0 && isSameCharType(text[currentPosition], isAlpha, isWhitespace)) {
      currentPosition--
    }

    // Stop after reaching the end of the previous word
    val wordEnd = currentPosition + 1


    // Ensure the selection and caret update appropriately
    if (wordEnd < caretModel.offset) {
      selectionModel.setSelection(wordEnd, caretModel.offset)
      caretModel.moveToOffset(wordEnd)
    }


  }
}


