package com.github.xvandexen.helixkeys.services.functionaltity

import com.github.xvandexen.helixkeys.services.ui.UiHandler
import com.github.xvandexen.helixkeys.startup.ModeAwareService
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer


@Service(Service.Level.PROJECT)
class ModeManager(project: Project):Disposable{

    private val uIHandler = UiHandler.getInstance(project)


    enum class Mode{
        NORMAL,
        INSERT
    }

    var currentMode = Mode.NORMAL
    private set


    fun toggleMode(mode: Mode){
        currentMode = when(mode){
            Mode.INSERT -> {
                thisLogger().debug("Switched To Special Mode")
                uIHandler.switchMode(mode)
                Mode.INSERT
            }
            Mode.NORMAL -> {
                thisLogger().debug("Switched To Normal Mode")
                uIHandler.switchMode(mode)
                Mode.NORMAL

            }
        }

    }

    override fun dispose() {
        Disposer.dispose(this)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ModeManager = project.service()
    }


}