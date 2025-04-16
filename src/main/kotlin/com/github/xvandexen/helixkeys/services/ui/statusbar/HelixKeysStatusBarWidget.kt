// HelixKeysStatusBarWidget.kt
package com.github.xvandexen.helixkeys.services.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import java.awt.Component
import java.awt.event.MouseEvent

class HelixKeysStatusBarWidget(private val project: Project) : StatusBarWidget, 
                                                              StatusBarWidget.TextPresentation {
    
    private val statusManager = StatusBarManager.getInstance(project)
    
    override fun ID(): String = StatusBarManager.WIDGET_ID
    
    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this
    
    override fun install(statusBar: StatusBar) { /* No initialization needed */ }
    
    override fun dispose() { /* No disposal needed */ }
    
    override fun getText(): String = statusManager.getText()
    
    override fun getAlignment(): Float = Component.LEFT_ALIGNMENT
    
    override fun getTooltipText(): String = "Active Mode (HelixKeys)"
    
    override fun getClickConsumer(): com.intellij.util.Consumer<MouseEvent>? = null
}