package com.github.xvandexen.helixkeys.services.functionaltity

import com.github.xvandexen.helixkeys.services.ui.statusbar.StatusBarManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer


@Service(Service.Level.PROJECT)
class ModeManager(val project: Project):Disposable{
    enum class Mode{
        NORMAL,
        INSERT,
        SELECT
    }

    var currentMode = Mode.NORMAL
    private set


    fun toggleMode(mode: Mode){
        currentMode = mode
        StatusBarManager.getInstance(project).setText(mode.name)

    }

    override fun dispose() {
        Disposer.dispose(this)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ModeManager = project.service()
    }


}