package com.github.xvandexen.helixkeys.ui

import com.github.xvandexen.helixkeys.configuration.KeyBindingConfig
import com.github.xvandexen.helixkeys.functionaltity.ModeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager

object UiHandler {
   private lateinit var  project: Project

  fun init(project: Project) {

    this.project = project
  }

  fun displayMenu(subBindings: Map<Set<Int>, KeyBindingConfig.RecKeyBinding>) {
      TODO("Not yet implemented")
    }
  fun switchMode(mode: ModeManager.Mode){
    when(mode){
      ModeManager.Mode.NORMAL -> HelixKeyStatusManager.updateText("NORMAL", project )
      ModeManager.Mode.INSERT -> HelixKeyStatusManager.updateText("INSERT", project)
    }
  }

  object HelixKeyStatusManager {
    var currentText: String = "INSERT"
      private set

    fun updateText(text: String, project: Project) {
      currentText = text
      // Update the widget in the UI
      WindowManager.getInstance().getStatusBar(project)?.updateWidget(HelixKeysStatusBarWidgetFactory.ID)
    }
  }

}
