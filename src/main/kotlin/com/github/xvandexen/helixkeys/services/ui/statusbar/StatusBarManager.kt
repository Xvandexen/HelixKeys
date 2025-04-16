// StatusBarManager.kt
package com.github.xvandexen.helixkeys.services.ui.statusbar

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.WindowManager

@Service(Service.Level.PROJECT)
class StatusBarManager(private val project: Project) {

  companion object {
    const val WIDGET_ID = "HelixKeys.StatusWidget"

    @JvmStatic
    fun getInstance(project: Project): StatusBarManager = project.service()
  }

  private var currentText = "Normal"

  fun setText(text: String) {
    currentText = text
    updateWidget()
  }

  fun getText(): String = currentText

  private fun getStatusBar(): StatusBar? =
    WindowManager.getInstance().getStatusBar(project)

  private fun updateWidget() {
    getStatusBar()?.updateWidget(WIDGET_ID)
  }
}