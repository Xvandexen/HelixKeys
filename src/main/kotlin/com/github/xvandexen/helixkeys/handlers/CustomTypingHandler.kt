package com.github.xvandexen.helixkeys.handlers

import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.TypedActionHandler

class CustomTypingHandler(private val originalHandler: TypedActionHandler) : TypedActionHandler {
    private val logger = thisLogger()
    override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
        if (editor.selectionModel.hasSelection()) {
            logger.info("CustomTypingHandler handling selection")
            val caretOffset = editor.caretModel.offset
            val selectionStart = editor.selectionModel.selectionStart
            val selectionEnd = editor.selectionModel.selectionEnd

            WriteCommandAction.runWriteCommandAction(editor.project) {
                // Insert the character at caret position
                editor.document.insertString(caretOffset, charTyped.toString())

                // Move caret one position forward
                editor.caretModel.moveToOffset(caretOffset + 1)

                // If typing happened before or at the selection start, adjust both boundaries
                if (caretOffset <= selectionStart) {
                    editor.selectionModel.setSelection(selectionStart + 1, selectionEnd + 1)
                }
                // If typing happened inside the selection, adjust only the end
                else if (caretOffset < selectionEnd) {
                    editor.selectionModel.setSelection(selectionStart, selectionEnd + 1)
                }
                // If typing happened at or after the selection end, don't change selection
                else {
                    editor.selectionModel.setSelection(selectionStart, selectionEnd)
                }
            }
        } else {
            logger.info("CustomTypingHandler delegating to original handler for non-selection typing")
            // For non-selection typing, use the original handler
            originalHandler.execute(editor, charTyped, dataContext)
        }
    }
}