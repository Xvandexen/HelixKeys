package com.github.xvandexen.helixkeys.actions.selectionActions

import com.github.xvandexen.helixkeys.actions.selectionActions.SelectionActions.findReferenceTarget
import com.github.xvandexen.helixkeys.actions.selectionActions.SelectionActions.selectElement
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch

class SelectReferencesToSymbolUnderCursor: AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val editor = FileEditorManager.getInstance(e.project!!).selectedTextEditor ?: return
    val project = e.project!!

    // Get the current file from the editor
    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return

    // Get the PSI element under the caret
    val offset = editor.caretModel.offset
    val element = psiFile.findElementAt(offset) ?: return

    // Find a referenceable target
    val target = findReferenceTarget(element) ?: return
    val originalElement = element.parent // Store the original element to ensure we include it

    // Find all references to this element in the current file
    val searchScope = LocalSearchScope(psiFile)
    val references = ReferencesSearch.search(target, searchScope).findAll()

    if (references.isEmpty()) {
      // If no references found, at least select the original symbol
      selectElement(editor, originalElement)
      return
    }

    // Clear existing carets and selections
    editor.caretModel.removeSecondaryCarets()
    val primaryCaret = editor.caretModel.primaryCaret

    // Make sure the original element is included
    var originalIncluded = false

    // Select all references
    references.forEachIndexed { index, ref ->
      val refElement = ref.element

      // Check if this reference contains or is the original element
      if (refElement == originalElement ||
        (originalElement.textRange.startOffset >= refElement.textRange.startOffset &&
                originalElement.textRange.endOffset <= refElement.textRange.endOffset)
      ) {
        originalIncluded = true
      }

      val range = refElement.textRange
      if (index == 0) {
        primaryCaret.moveToOffset(range.startOffset)
        primaryCaret.setSelection(range.startOffset, range.endOffset)
      } else {
        val position = editor.offsetToVisualPosition(range.startOffset)
        val caret = editor.caretModel.addCaret(position) ?: return@forEachIndexed
        caret.setSelection(range.startOffset, range.endOffset)
      }
    }

    // If the original element wasn't included in the references, add it

    if (!originalIncluded) {
      val range = originalElement.textRange
      val position = editor.offsetToVisualPosition(range.startOffset)
      val caret = editor.caretModel.addCaret(position) ?: return
      caret.setSelection(range.startOffset, range.endOffset)

    }


  }
}