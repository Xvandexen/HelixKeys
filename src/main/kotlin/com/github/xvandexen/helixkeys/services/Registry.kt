package com.github.xvandexen.helixkeys.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

/**
 * Service that manages registers for editors in the project.
 * 
 * In text editors like Helix, registers are used to store text for operations
 * like yank (copy) and paste. This service maintains a mapping between Editor
 * instances and Register objects, allowing each editor to have its own set of registers.
 *
 * @property project The current project instance
 */
@Service(Service.Level.PROJECT)
class Registry(private val project: Project) {
  /**
   * Data class representing a register for an editor.
   * 
   * Registers store yanked text, user-defined registers, an anchor position,
   * and selection history for operations in the editor.
   *
   * @property mainYankReg The main yank register that stores copied text
   * @property userYankReg Map of user-defined registers identified by characters
   * @property anchor The current anchor position in the editor, used for certain operations
   * @property selectionHistory List of previous selections as pairs of start and end positions
   */
  data class Register(
    var mainYankReg: String = "",
    var userYankReg: MutableMap<Char, String> = mutableMapOf(),
    var anchor: Int? = null,
    val selectionHistory: MutableList<Pair<Int,Int>> = mutableListOf()
  ) {
    /**
     * Updates the anchor position.
     *
     * @param offset The new anchor position in the editor
     */
    fun updateAnchor(offset: Int) {
      anchor = offset;
    }

    /**
     * Adds a selection to the history.
     *
     * @param start The start offset of the selection
     * @param end The end offset of the selection
     */
    fun addSelectionToHistory(start: Int, end: Int) {
      selectionHistory.add(Pair(start, end))
    }
  }

  /**
   * Companion object providing access to the Registry service instance.
   */
  companion object {
    /**
     * Gets the Registry service instance for the specified project.
     *
     * @param project The project for which to get the Registry service
     * @return The Registry service instance
     */
    @JvmStatic
    fun getInstance(project: Project): Registry = project.service()
  }

  /**
   * Map that associates Editor instances with their corresponding Register objects.
   * Uses ConcurrentHashMap for thread safety.
   */
  private val editorsAndRegisters = ConcurrentHashMap<Editor, Register>()

  /**
   * Gets the Register object for the specified editor.
   *
   * @param editor The editor for which to get the register
   * @return The Register object for the editor, or null if no register exists for the editor
   */
  fun getRegister(editor: Editor): Register? {
    return editorsAndRegisters.get(editor)
  }

  /**
   * Adds a new Register object for the specified editor.
   *
   * @param editor The editor for which to add a register
   */
  fun addRegistry(editor: Editor){
    editorsAndRegisters.put(editor,Register())
  }


}
