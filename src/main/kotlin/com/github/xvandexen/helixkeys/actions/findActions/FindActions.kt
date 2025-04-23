package com.github.xvandexen.helixkeys.actions.findActions

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.github.xvandexen.helixkeys.services.utilities.NotificationErrorHandling
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx

/**
 * Utility object providing methods for finding characters in text.
 * 
 * This object contains functionality for finding characters in different directions
 * (forward/backward) and with different inclusivity options (inclusive/exclusive).
 * It supports the various find actions in the Helix editor.
 */
object FindActions {
  /**
   * Data class representing the start and found positions in a text search.
   *
   * @property start The starting position of the search
   * @property found The position where the character was found
   */
  data class Positions(val start: Int, val found: Int)

  /**
   * Enumeration of different types of character finding operations.
   * 
   * Each type has a hint text to display to the user, an inclusivity flag,
   * and a direction flag.
   *
   * @property hintText The text to display as a hint to the user
   * @property inclusive Whether the found character should be included in the selection
   * @property forward Whether to search forward or backward in the text
   */
  enum class FindType(val hintText: String, val inclusive: Boolean, val forward: Boolean) {
    TILL_NEXT("Find 'Till Next Char:", false, true),
    NEXT("Find Next Char:", true, true),
    TILL_PREV("Find 'Till Prev Char:", false, false),
    PREV("Find Prev Char:", true, false)
  }






  //TODO(ADD Escape option)
  /**
   * Sets up a character finder that captures the next typed character and finds its occurrence.
   * 
   * This method shows a hint to the user, then sets up a temporary handler to capture
   * the next character typed. Once the character is typed, it finds the occurrence of that
   * character in the text according to the specified find type, selects the text between
   * the current position and the found character, and moves the caret.
   *
   * @param executor The CommandExecutor instance
   * @param findType The type of find operation to perform (NEXT, PREV, TILL_NEXT, TILL_PREV)
   */
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

  /**
   * Finds a character in the current line of text.
   * 
   * This method searches for the specified character in the text, either forward or backward
   * from the current caret position, and returns the start and found positions.
   *
   * @param executor The CommandExecutor instance
   * @param editor The editor containing the text
   * @param typedChar The character to find
   * @param forward Whether to search forward (true) or backward (false)
   * @return A Positions object containing the start and found positions
   */
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

  /**
   * Finds the next occurrence of a character in the text.
   * 
   * This method searches forward from the start position for the specified character
   * and returns the start and found positions.
   *
   * @param text The text to search in
   * @param typedChar The character to find
   * @param startPos The position to start searching from
   * @return A Positions object containing the start and found positions
   */
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

  /**
   * Finds the previous occurrence of a character in the text.
   * 
   * This method searches backward from the start position for the specified character
   * and returns the start and found positions.
   *
   * @param text The text to search in
   * @param typedChar The character to find
   * @param startPos The position to start searching from
   * @return A Positions object containing the start and found positions
   */
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
