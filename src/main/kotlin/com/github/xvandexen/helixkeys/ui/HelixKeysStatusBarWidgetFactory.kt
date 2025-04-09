package com.github.xvandexen.helixkeys.ui

import com.intellij.openapi.wm.WindowManager
import com.github.xvandexen.helixkeys.functionaltity.ModeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import java.awt.Component
import java.awt.event.MouseEvent

class HelixKeysStatusBarWidgetFactory : StatusBarWidgetFactory {
  companion object {
    const val ID = "HelixKeys.StatusWidget"
  }

  private var currentText = "Insert"

  override fun getId(): String = ID

  override fun getDisplayName(): String = "HelixKeys"

  override fun isAvailable(project: Project): Boolean = true

  override fun createWidget(project: Project): StatusBarWidget {
    return MyStatusBarWidget(currentText)
  }

  override fun disposeWidget(widget: StatusBarWidget) {
    // Clean up resources if needed
  }

  override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true



  private class MyStatusBarWidget(private val text: String) : StatusBarWidget, StatusBarWidget.TextPresentation {
    override fun ID(): String = ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {
      // Initialization if needed
    }

    override fun dispose() {
      // Clean up resources
    }

    override fun getText(): String = text

    override fun getAlignment(): Float = Component.LEFT_ALIGNMENT

    override fun getTooltipText(): String? = "Active Mode(HelixKeys)"

    override fun getClickConsumer(): com.intellij.util.Consumer<MouseEvent>? = null // Handle clicks if needed
  }



};