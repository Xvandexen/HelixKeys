package com.github.xvandexen.helixkeys.services.ui

import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig
import com.github.xvandexen.helixkeys.services.configuration.KeyCombo
import com.github.xvandexen.helixkeys.services.ui.commandoverlay.CommandOverlayPanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer

/**
 * Service responsible for managing the command overlay UI.
 * 
 * This service creates and manages a CommandOverlayPanel instance,
 * providing methods to show and hide key bindings in the overlay panel.
 * It acts as a facade for the UI component, simplifying interaction with it.
 *
 * @property project The current project instance
 */
@Service(Service.Level.PROJECT)
class CommandOverlayService(private val project: Project) : Disposable {

    /**
     * The overlay panel instance, lazily initialized.
     * 
     * This property is initialized only when first accessed, and the panel
     * is registered for disposal with this service.
     */
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
     * Shows key bindings in the overlay panel.
     * 
     * This method delegates to the overlay panel to display the provided
     * key bindings in a semi-transparent overlay in the editor.
     *
     * @param subBindings The map of key combinations to their corresponding bindings to display
     */
    fun showBindings(subBindings: Map<KeyCombo, KeyBindingConfig.RecKeyBinding>) {
        overlayPanel.showBindings(subBindings)
    }

    /**
     * Hides the overlay panel.
     * 
     * This method delegates to the overlay panel to hide it and remove it
     * from the editor's layered pane.
     */
    fun hideBindings() {
        overlayPanel.hidePanel()
    }

    /**
     * Companion object providing access to the CommandOverlayService instance.
     */
    companion object {
        /**
         * Gets the CommandOverlayService instance for the specified project.
         *
         * @param project The project for which to get the CommandOverlayService
         * @return The CommandOverlayService instance
         */
        @JvmStatic
        fun getInstance(project: Project): CommandOverlayService = project.service()
    }

    /**
     * Disposes of resources used by this service.
     * 
     * This method is called when the service is being shut down.
     */
    override fun dispose() {
        Disposer.dispose(this)
    }
}
