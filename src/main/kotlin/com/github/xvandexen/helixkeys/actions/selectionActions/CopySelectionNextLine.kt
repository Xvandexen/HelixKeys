package com.github.xvandexen.helixkeys.actions.selectionActions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import kotlin.math.min

class CopySelectionNextLine :AnAction(){



  override fun actionPerformed(e: AnActionEvent) {

      val editor = e.getRequiredData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR)
      val caretModel = editor.caretModel
      val document = editor.document

      // Store the current carets in a separate list to avoid concurrent modification
      val currentCarets = ArrayList(caretModel.allCarets)

      for (caret in currentCarets) {
        val selectionStart = caret.selectionStart
        val selectionEnd = caret.selectionEnd

        // Get the current caret position
        val caretOffset = caret.offset

        // Calculate the offset to move to the next line
        val currentLine = document.getLineNumber(caretOffset)
        val nextLine = currentLine + 1

        // Make sure we're not already at the last line
        if (nextLine < document.lineCount) {
          // Calculate column positions
          val currentLineStartOffset = document.getLineStartOffset(currentLine)
          val nextLineStartOffset = document.getLineStartOffset(nextLine)

          // Calculate the offsets relative to start of line
          val selectionStartCol = selectionStart - currentLineStartOffset
          val selectionEndCol = selectionEnd - currentLineStartOffset

          // Calculate the caret column relative to start of line
          val caretCol = caretOffset - currentLineStartOffset

          // Calculate new selection positions, ensuring we don't go past end of line
          val nextLineEndOffset = document.getLineEndOffset(nextLine)
          val nextLineLength = nextLineEndOffset - nextLineStartOffset

          val newSelectionStart = nextLineStartOffset + min(selectionStartCol, nextLineLength)
          val newSelectionEnd = nextLineStartOffset + min(selectionEndCol, nextLineLength)

          // Calculate where to place the new caret - at same column as original or at line end
          val newCaretOffset = if (caretCol <= nextLineLength) {
            nextLineStartOffset + caretCol
          } else {
            nextLineEndOffset // Place at end of line if original column exceeds line length
          }

          // Create a new caret at the corresponding position on the next line
          caretModel.addCaret(editor.offsetToVisualPosition(newCaretOffset), true)
          val newCaret = caretModel.allCarets.last()
          newCaret.setSelection(newSelectionStart, newSelectionEnd)
        }


      }
    }
  }

