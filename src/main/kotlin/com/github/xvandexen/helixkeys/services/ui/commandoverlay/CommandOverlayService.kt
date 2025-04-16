package com.github.xvandexen.helixkeys.services.ui

import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig
import com.github.xvandexen.helixkeys.services.configuration.KeyCombo
import com.github.xvandexen.helixkeys.services.ui.commandoverlay.CommandOverlayPanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer

@Service(Service.Level.PROJECT)
class CommandOverlayService(private val project: Project) : Disposable {

    private val overlayPanel by lazy {
        CommandOverlayPanel(project).also {
            Disposer.register(this, it)
        }
    }

    init {
        // Register for disposal with the project
        Disposer.register(project, overlayPanel)
    }

    /**
     * Shows bindings in the overlay panel
     */
    fun showBindings(subBindings: Map<KeyCombo, KeyBindingConfig.RecKeyBinding>) {
        overlayPanel.showBindings(subBindings)
    }

    /**
     * Hides the overlay panel
     */
    fun hideBindings() {
        overlayPanel.hidePanel()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CommandOverlayService = project.service()
    }


    override fun dispose() {
        Disposer.dispose(this)
    }
}