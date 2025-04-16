// HelixKeysStatusBarWidgetFactory.kt
package com.github.xvandexen.helixkeys.services.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class HelixKeysStatusBarWidgetFactory : StatusBarWidgetFactory {
    
    override fun getId(): String = StatusBarManager.WIDGET_ID
    
    override fun getDisplayName(): String = "HelixKeys"
    
    override fun isAvailable(project: Project): Boolean = true
    
    override fun createWidget(project: Project): StatusBarWidget = 
        HelixKeysStatusBarWidget(project)
    
    override fun disposeWidget(widget: StatusBarWidget) { /* No cleanup needed */ }
    
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}