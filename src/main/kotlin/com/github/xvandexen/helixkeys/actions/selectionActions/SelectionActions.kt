package com.github.xvandexen.helixkeys.actions.selectionActions

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import kotlin.math.min

object SelectionActions {





  // Helper function to select a single element
  fun selectElement(editor: Editor, element: PsiElement) {
    val range = element.textRange
    editor.caretModel.primaryCaret.setSelection(range.startOffset, range.endOffset)
  }

  fun findReferenceTarget(element: PsiElement): PsiElement? {
    var current: PsiElement? = element

    while (current != null) {
      // Check if this element has references to it
      if (current is PsiNamedElement && current.name != null) {
        return current
      }

      // If element is a reference, resolve it
      val ref = current.references.firstOrNull()
      if (ref != null) {
        val resolved = ref.resolve()
        if (resolved != null) {
          return resolved
        }
      }

      current = current.parent
    }

    return null
  }
}



