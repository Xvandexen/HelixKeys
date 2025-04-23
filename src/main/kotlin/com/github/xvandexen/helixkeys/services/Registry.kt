package com.github.xvandexen.helixkeys.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class Registry(private val project: Project) {
  data class Register(
    var anchor: Int? = null
  ) {
    fun updateAnchor(offset: Int) {
      anchor = offset;
    }

    fun addSelectionToHistory(lineStartOffset: Int, lineEndOffset: Int) {
      TODO("Not yet implemented")
    }
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): Registry = project.service()
  }

  private val editorsAndRegisters = ConcurrentHashMap<Editor, Register>()

  fun getRegister(editor: Editor): Register? {
    return editorsAndRegisters.get(editor)
  }

  fun addRegistry(editor: Editor){
    editorsAndRegisters.put(editor,Register())
  }


}