package com.github.xvandexen.helixkeys.services.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.WindowManager
import java.awt.Component
import java.awt.event.MouseEvent

// Define a service to maintain state
@Service(Service.Level.PROJECT)
class HelixKeysStatusService(private val project: Project): Disposable {

  private val ID = "HelixKeys.StatusWidget"
  var currentText = "Normal"

  companion object {
    @JvmStatic
    fun getInstance(project: Project): HelixKeysStatusService = project.service()
  }

  fun updateText(text: String) {
    currentText = text
    refreshWidgets()
  }

  private fun refreshWidgets() {
    val statusBar = WindowManager.getInstance().getStatusBar(project)
    statusBar?.updateWidget(ID)
  }

  override fun dispose() {
    Disposer.dispose(this)
  }
}

class HelixKeysStatusBarWidgetFactory : StatusBarWidgetFactory {

  companion object {
    const val ID = "HelixKeys.StatusWidget"
  }

  override fun getId(): String = ID

  override fun getDisplayName(): String = "HelixKeys"

  override fun isAvailable(project: Project): Boolean = true

  override fun createWidget(project: Project): StatusBarWidget = MyStatusBarWidget(project)

  override fun disposeWidget(widget: StatusBarWidget) {
    // Clean up resources if needed
  }

  override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true

  private class MyStatusBarWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation {

    private val service: HelixKeysStatusService = HelixKeysStatusService.getInstance(project)

    override fun ID(): String = ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {
      // Initialization if needed
    }

    override fun dispose() {
      Disposer.dispose(this)
    }

    // Get the current text from the service
    override fun getText(): String = service.currentText

    override fun getAlignment(): Float = Component.LEFT_ALIGNMENT

    override fun getTooltipText(): String? = "Active Mode (HelixKeys)"

    override fun getClickConsumer(): com.intellij.util.Consumer<MouseEvent>? = null
  }
}
