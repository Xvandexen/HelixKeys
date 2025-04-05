package com.github.xvandexen.helixkeys.services

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

class ModeManager(private val project: Project) {
    companion object{
        val MODE_CHANGE_TOPIC = Topic.create("MODE_CHANGE_TOPIC", ModeChangeListener::class.java)
    }
    interface ModeChangeListener{
        fun modeChanged()
    }

    enum class Mode{
        NORMAL,
        SPECIAL
    }

var currentMode = Mode.NORMAL
    private set

    fun toggleMode(){
        currentMode= when(currentMode){
            Mode.NORMAL -> {
                thisLogger().debug("Switched To Special Mode")
                Mode.SPECIAL
            }
            Mode.SPECIAL -> {
                thisLogger().debug("Switched To Normal Mode")
                Mode.NORMAL

            }
        }

        project.messageBus.syncPublisher(MODE_CHANGE_TOPIC).modeChanged();
    }

    fun isSpecialMode() = currentMode == Mode.SPECIAL
}