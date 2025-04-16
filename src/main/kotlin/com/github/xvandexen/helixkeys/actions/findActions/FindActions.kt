package com.github.xvandexen.helixkeys.actions.findActions

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.github.xvandexen.helixkeys.services.utilities.NotificationErrorHandling
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx

object FindActions {
  data class Positions(val start: Int, val found: Int)


  enum class FindType(val hintText: String, val inclusive: Boolean, val forward: Boolean) {
    TILL_NEXT("Find 'Till Next Char:", false, true),
    NEXT("Find Next Char:", true, true),
    TILL_PREV("Find 'Till Prev Char:", false, false),
    PREV("Find Prev Char:", true, false)
  }






  //TODO(ADD Escape option)
  fun setupCharacterFinder(executor: CommandExecutor, findType: FindType) {

    val editor = FileEditorManagerEx.getInstanceEx(executor.project).selectedTextEditor ?: return
    val originalHandler = TypedAction.getInstance().rawHandler

    // Show a hint to the user
    HintManager.getInstance().showInformationHint(editor, findType.hintText)

    // Set up a temporary handler to capture the next typed character
    TypedAction.getInstance().setupRawHandler { editor, typedChar, dataContext ->
      TypedAction.getInstance().setupRawHandler(originalHandler)

      // Find the character and move the caret
      val pos = findCharInLine(executor, editor, typedChar, findType.forward)
      val endOffset = if (findType.inclusive) pos.found + 1 else pos.found

      editor.selectionModel.setSelection(pos.start, endOffset)
      editor.caretModel.primaryCaret.moveToOffset(endOffset)
    }
  }

  private fun findCharInLine(executor: CommandExecutor, editor: Editor, typedChar: Char, forward: Boolean): Positions {

    val document = editor.document
    val caretModel = editor.caretModel
    val offset = caretModel.offset
    val text = document.text
    val startPos = offset

    if (startPos >= text.length || startPos < 0) return Positions(startPos, startPos)

    try {
      return if (forward) {
        findForward(text, typedChar, startPos)
      } else {
        findBackward(text, typedChar, startPos)
      }
    } catch (e: Exception) {
      // In case of any error, just log it
      NotificationErrorHandling.showErrorNotification(
        executor.project,
        "Find Character Failed",
        e.toString()
      )
      return Positions(startPos, startPos)
    }
  }

  private fun findForward(text: String, typedChar: Char, startPos: Int): Positions {
    var currentPos = startPos + 1 // Start searching from next position

    // Find the next occurrence of the character
    while (currentPos < text.length && text[currentPos] != typedChar) {
      currentPos++
    }

    // If found, return the position
    return if (currentPos < text.length && text[currentPos] == typedChar) {
      Positions(startPos, currentPos)
    } else {
      Positions(startPos, startPos)
    }
  }

  private fun findBackward(text: String, typedChar: Char, startPos: Int): Positions {
    var currentPos = startPos - 1 // Start searching from previous position

    // Find the previous occurrence of the character
    while (currentPos >= 0 && text[currentPos] != typedChar) {
      currentPos--
    }

    // If found, return the position
    return if (currentPos >= 0 && text[currentPos] == typedChar) {
      Positions(startPos, currentPos)
    } else {
      Positions(startPos, startPos)
    }
  }
}