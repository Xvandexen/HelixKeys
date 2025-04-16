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

object MoveActions {

  data class EditorComponents(
    val caretModel: CaretModel,
    val document: Document,
    val selectionModel: SelectionModel,
    val text: String
  )

  // Obtain editor components for all movement operations
   fun getEditorComponents(project: Project): EditorComponents? {
    val editor : Editor = FileEditorManagerEx.getInstanceEx(project).selectedTextEditor?: return null


    return EditorComponents(
      caretModel = editor.caretModel,
      document = editor.document,
      selectionModel = editor.selectionModel,
      text = editor.document.text
    )
  }

  // Check if a character is whitespace
   fun isWhitespace(char: Char) = Character.isWhitespace(char)

  // Check if two characters belong to the same general type (alpha or not)
   fun isSameCharType(currentChar: Char, startAlpha: Boolean, startWhitespace: Boolean): Boolean {
    return when {
      startWhitespace -> isWhitespace(currentChar)
      startAlpha -> Character.isLetter(currentChar)
      else -> !Character.isLetter(currentChar) && !isWhitespace(currentChar)
    }
  }
}