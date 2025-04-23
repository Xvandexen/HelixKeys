package com.github.xvandexen.helixkeys.actions.moveActions

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project

/**
 * Utility object providing methods for handling movement operations in the editor.
 * 
 * This object contains functionality for retrieving editor components and
 * performing character type checks that are commonly used by various movement actions.
 */
object MoveActions {

  /**
   * Data class containing the essential editor components needed for movement operations.
   *
   * @property caretModel The caret model for cursor positioning
   * @property document The document being edited
   * @property selectionModel The selection model for text selection
   * @property text The text content of the document
   */
  data class EditorComponents(
    val caretModel: CaretModel,
    val document: Document,
    val selectionModel: SelectionModel,
    val text: String
  )

  /**
   * Retrieves the essential editor components needed for movement operations.
   * 
   * This method gets the currently selected text editor and extracts the caret model,
   * document, selection model, and text content, packaging them into an EditorComponents object.
   *
   * @param project The current project
   * @return An EditorComponents object containing the editor components, or null if no editor is selected
   */
   fun getEditorComponents(project: Project): EditorComponents? {
    val editor : Editor = FileEditorManagerEx.getInstanceEx(project).selectedTextEditor?: return null


    return EditorComponents(
      caretModel = editor.caretModel,
      document = editor.document,
      selectionModel = editor.selectionModel,
      text = editor.document.text
    )
  }

  /**
   * Checks if a character is whitespace.
   * 
   * This is a convenience method that delegates to Character.isWhitespace.
   *
   * @param char The character to check
   * @return True if the character is whitespace, false otherwise
   */
   fun isWhitespace(char: Char) = Character.isWhitespace(char)

  /**
   * Checks if a character belongs to the same general type as a reference character.
   * 
   * This method determines if a character belongs to the same category (letter, whitespace,
   * or other) as specified by the startAlpha and startWhitespace parameters.
   *
   * @param currentChar The character to check
   * @param startAlpha Whether the reference character is a letter
   * @param startWhitespace Whether the reference character is whitespace
   * @return True if the character belongs to the same category, false otherwise
   */
   fun isSameCharType(currentChar: Char, startAlpha: Boolean, startWhitespace: Boolean): Boolean {
    return when {
      startWhitespace -> isWhitespace(currentChar)
      startAlpha -> Character.isLetter(currentChar)
      else -> !Character.isLetter(currentChar) && !isWhitespace(currentChar)
    }
  }
}
