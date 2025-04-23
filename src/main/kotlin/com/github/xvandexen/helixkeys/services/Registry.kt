package com.github.xvandexen.helixkeys.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class Registry(private val project: Project) {
  data class Register(
    var mainYankReg: String = "",
    var userYankReg: MutableMap<Char, String> = mutableMapOf(),
    var anchor: Int? = null,
    val selectionHistory: MutableList<Pair<Int,Int>> = mutableListOf()
  ) {
    fun updateAnchor(offset: Int) {
      anchor = offset;
    }

    fun addSelectionToHistory(start: Int, end: Int) {
      selectionHistory.add(Pair(start, end))

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