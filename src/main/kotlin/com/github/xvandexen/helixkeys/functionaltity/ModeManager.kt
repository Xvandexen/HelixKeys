package com.github.xvandexen.helixkeys.functionaltity

import com.github.xvandexen.helixkeys.ui.UiHandler
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

object ModeManager{



    enum class Mode{
        NORMAL,
        INSERT
    }

    var currentMode = Mode.NORMAL
    private set


    fun toggleMode(mode: Mode){
        currentMode= when(mode){
            Mode.INSERT -> {
                thisLogger().debug("Switched To Special Mode")
                UiHandler.switchMode(mode)
                Mode.INSERT
            }
            Mode.NORMAL -> {
                thisLogger().debug("Switched To Normal Mode")
                UiHandler.switchMode(mode)
                Mode.NORMAL

            }
        }

    }


}