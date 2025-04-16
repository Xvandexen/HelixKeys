package com.github.xvandexen.helixkeys.actions.moveActions

import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.getEditorComponents
import com.github.xvandexen.helixkeys.actions.moveActions.MoveActions.isWhitespace
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class LongPrevWordStart: AnAction() {


  override fun actionPerformed(e: AnActionEvent) {
    val components = getEditorComponents(e.project!!)?: return
    val (caretModel, _, _, text) = components
    val currentPosition = caretModel.offset

    var newOffset = currentPosition
    while (newOffset > 0 && isWhitespace(text[newOffset - 1])) {
      newOffset--
    }
    while (newOffset > 0 && !isWhitespace(text[newOffset - 1])) {
      newOffset--
    }

    if (newOffset != currentPosition) {
      caretModel.moveToOffset(newOffset)
    }

  }


}