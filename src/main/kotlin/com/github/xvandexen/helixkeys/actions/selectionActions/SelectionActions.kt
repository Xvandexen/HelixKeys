package com.github.xvandexen.helixkeys.actions.selectionActions

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import kotlin.math.min

/**
 * Utility object providing methods for handling selection operations in the editor.
 * 
 * This object contains functionality for selecting PSI elements and finding
 * referenceable targets that are used by various selection actions.
 */
object SelectionActions {

  /**
   * Selects a single PSI element in the editor.
   * 
   * This method sets the primary caret's selection to cover the text range of the specified element.
   *
   * @param editor The editor in which to make the selection
   * @param element The PSI element to select
   */
  fun selectElement(editor: Editor, element: PsiElement) {
    val range = element.textRange
    editor.caretModel.primaryCaret.setSelection(range.startOffset, range.endOffset)
  }

  /**
   * Finds a referenceable target for a PSI element.
   * 
   * This method traverses up the PSI tree from the given element, looking for a named element
   * that can be referenced. It also attempts to resolve references if the element is a reference.
   *
   * @param element The PSI element to find a referenceable target for
   * @return A referenceable PSI element, or null if none is found
   */
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
