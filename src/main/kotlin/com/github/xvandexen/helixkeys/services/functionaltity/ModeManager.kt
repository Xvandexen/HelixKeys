package com.github.xvandexen.helixkeys.services.functionaltity

import com.github.xvandexen.helixkeys.services.ui.statusbar.StatusBarManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer

/**
 * Service responsible for managing the editor mode.
 * 
 * This class tracks the current editor mode (normal, insert, select),
 * provides methods to toggle between modes, and updates the status bar
 * to reflect the current mode.
 *
 * @property project The current project instance
 */
@Service(Service.Level.PROJECT)
class ModeManager(val project: Project): Disposable {
    /**
     * Enumeration of available editor modes.
     * 
     * These modes correspond to the different interaction modes in the Helix editor:
     * - NORMAL: Default mode for navigation and commands
     * - INSERT: Mode for inserting and editing text
     * - SELECT: Mode for selecting text
     */
    enum class Mode {
        NORMAL,
        INSERT,
        SELECT
    }

    /**
     * The current editor mode.
     * 
     * This property is read-only from outside the class and can only be
     * changed through the toggleMode method.
     */
    var currentMode = Mode.NORMAL
        private set

    /**
     * Changes the current editor mode and updates the status bar.
     * 
     * This method sets the current mode to the specified mode and
     * updates the status bar to display the new mode name.
     *
     * @param mode The mode to switch to
     */
    fun toggleMode(mode: Mode) {
        currentMode = mode
        StatusBarManager.getInstance(project).setText(mode.name)
    }

    /**
     * Disposes of resources used by this service.
     * 
     * This method is called when the service is being shut down.
     */
    override fun dispose() {
        Disposer.dispose(this)
    }

    /**
     * Companion object providing access to the ModeManager service instance.
     */
    companion object {
        /**
         * Gets the ModeManager service instance for the specified project.
         *
         * @param project The project for which to get the ModeManager service
         * @return The ModeManager service instance
         */
        @JvmStatic
        fun getInstance(project: Project): ModeManager = project.service()
    }
}
