package com.github.xvandexen.helixkeys.services.commands

import com.intellij.openapi.editor.Editor
import com.jetbrains.rd.framework.base.deepClonePolymorphic
import com.jetbrains.rd.generator.nova.PredefinedType
import org.bouncycastle.util.Characters

object MoveActions {


  fun nextWordStart(executor: CommandExecutor) {
    // Get editor instance
    val editor = executor.editor
    // Get document and caret model
    val document = editor.document
    val caretModel = editor.caretModel
    val selectionModel = editor.selectionModel
    // Get current caret position
    val offset = caretModel.offset
    // Get text content
    val text = document.text

    // Start from current position
    val startPos = offset

    // Check if we're at the end of the document
    if (startPos >= text.length) return

    // Determine the character type at the starting position
    val startChar = text[startPos]
    val isStartAlpha = Character.isLetter(startChar)
    val isStartWhitespace = Character.isWhitespace(startChar)

    // Select characters of the same type
    var endPos = startPos

    while (endPos < text.length) {
      val currentChar = text[endPos]

      // If we encounter whitespace, stop
      if (Character.isWhitespace(currentChar) && !isStartWhitespace) {
        break
      }

      // If the character type changes, stop
      if (isStartAlpha && !Character.isLetter(currentChar)) {
        break
      } else if (!isStartAlpha && Character.isLetter(currentChar)) {
        break
      }

      endPos++
    }

    // Only create a selection if we found characters of the same type
    if (endPos > startPos) {
      selectionModel.setSelection(startPos, endPos)
      caretModel.moveToOffset(endPos)
    }
  }

  fun prevWordStart(executor: CommandExecutor) {
    // Get editor instance
    val editor = executor.editor
    // Get document and caret model
    val document = editor.document
    val caretModel = editor.caretModel
    val selectionModel = editor.selectionModel
    // Get current caret position
    val offset = caretModel.offset
    // Get text content
    val text = document.text

    // Start from current position
    val endPos = offset

    // Check if we're at the beginning of the document
    if (endPos <= 0) return

    // Determine the character type at the position before current
    val startPos = endPos - 1
    if (startPos < 0) return

    val startChar = text[startPos]
    val isStartAlpha = Character.isLetter(startChar)
    val isStartWhitespace = Character.isWhitespace(startChar)

    // Select characters of the same type going backward
    var currentPos = startPos

    while (currentPos >= 0) {
      val currentChar = text[currentPos]

      // If we encounter whitespace, stop
      if (Character.isWhitespace(currentChar) && !isStartWhitespace) {
        break
      }

      // If the character type changes, stop
      if (isStartAlpha && !Character.isLetter(currentChar)) {
        break
      } else if (!isStartAlpha && Character.isLetter(currentChar)) {
        break
      }

      currentPos--
    }

    // We went one character too far back, so adjust
    val selectionStart = currentPos + 1

    // Only create a selection if we found characters of the same type
    if (selectionStart < endPos) {
      selectionModel.setSelection(selectionStart, endPos)
      caretModel.moveToOffset(selectionStart)
    }
  }
  fun nextWordEnd(executor: CommandExecutor) {
    // Get editor instance
    val editor = executor.editor
    // Get document and caret model
    val document = editor.document
    val caretModel = editor.caretModel
    val selectionModel = editor.selectionModel
    // Get current caret position
    val offset = caretModel.offset
    // Get text content
    val text = document.text

    // Start from current position
    val startPos = offset

    // Check if we're at the end of the document
    if (startPos >= text.length) return

    // First, skip any characters of the same type as the current one
    var currentPos = startPos

    // Determine the character type at the starting position
    val startChar = text[currentPos]
    val isStartAlpha = Character.isLetter(startChar)
    val isStartWhitespace = Character.isWhitespace(startChar)

    // Skip past characters of the current type
    while (currentPos < text.length) {
      val currentChar = text[currentPos]

      // If we encounter whitespace and didn't start with whitespace, stop
      if (Character.isWhitespace(currentChar) && !isStartWhitespace) {
        break
      }

      // If the character type changes, stop
      if (isStartAlpha && !Character.isLetter(currentChar)) {
        break
      } else if (!isStartAlpha && Character.isLetter(currentChar)) {
        break
      }

      currentPos++
    }

    // If we've already reached the end of the text or a whitespace, we're done
    if (currentPos >= text.length || Character.isWhitespace(text[currentPos])) {
      // Skip any following whitespace
      while (currentPos < text.length && Character.isWhitespace(text[currentPos])) {
        currentPos++
      }
    }

    // Now, if we're not at the end, look for the end of the next word
    if (currentPos < text.length) {
      // Find the type of the next non-whitespace character
      val nextChar = text[currentPos]
      val isNextAlpha = Character.isLetter(nextChar)

      // Continue until we find a different character type or whitespace
      var endPos = currentPos
      while (endPos < text.length) {
        val currChar = text[endPos]

        // If we hit whitespace, stop
        if (Character.isWhitespace(currChar)) {
          break
        }

        // If character type changes, stop
        if (isNextAlpha && !Character.isLetter(currChar)) {
          break
        } else if (!isNextAlpha && Character.isLetter(currChar)) {
          break
        }

        endPos++
      }

      // Update selection and caret
      selectionModel.setSelection(startPos, endPos)
      caretModel.moveToOffset(endPos)
    } else {
      // If we're at the end after skipping, just set the caret there
      selectionModel.setSelection(startPos, currentPos)
      caretModel.moveToOffset(currentPos)
    }
  }

  fun prevWordEnd(executor: CommandExecutor) {
    // Get editor instance
    val editor = executor.editor
    // Get document and caret model
    val document = editor.document
    val caretModel = editor.caretModel
    val selectionModel = editor.selectionModel
    // Get current caret position
    val offset = caretModel.offset
    // Get text content
    val text = document.text

    // Start from current position
    var currentPos = offset

    // Check if we're at the beginning of the document
    if (currentPos <= 0) return

    // First, step back one position to start checking from the previous character
    currentPos--

    // If we're at a whitespace character, skip all whitespace going backward
    if (currentPos >= 0 && Character.isWhitespace(text[currentPos])) {
      while (currentPos >= 0 && Character.isWhitespace(text[currentPos])) {
        currentPos--
      }
    }

    // If we've reached the beginning, set caret at the beginning
    if (currentPos < 0) {
      selectionModel.setSelection(0, offset)
      caretModel.moveToOffset(0)
      return
    }

    // Determine the character type at the current position (which is the end of a word)
    val endCharType = Character.isLetter(text[currentPos])

    // Find the beginning of this word by going backward until character type changes
    var wordEnd = currentPos
    while (wordEnd >= 0) {
      val currentChar = text[wordEnd]

      // If we hit whitespace, stop
      if (Character.isWhitespace(currentChar)) {
        break
      }

      // If character type changes, stop
      if (endCharType != Character.isLetter(currentChar)) {
        break
      }

      wordEnd--
    }

    // We've gone one character too far back
    wordEnd++

    // Create the selection from wordEnd to initial position
    selectionModel.setSelection(wordEnd, offset)
    caretModel.moveToOffset(wordEnd)
  }

}