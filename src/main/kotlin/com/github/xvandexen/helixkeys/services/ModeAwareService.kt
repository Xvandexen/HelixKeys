package com.github.xvandexen.helixkeys.services

import com.esotericsoftware.kryo.kryo5.minlog.Log
import com.github.xvandexen.helixkeys.functionaltity.ModeSensitiveHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class ModeAwareService(private val project: Project): Disposable {
    private var modeManager: ModeManager
    private var keyHandler: ModeSensitiveHandler

    init {
        thisLogger().info("[HelixKeys] Initializing ModeAwareService for project: ${project.name}")

        modeManager = ModeManager(project)
        keyHandler = ModeSensitiveHandler(modeManager,project)


    }

    override fun dispose() {
        println("[HelixKeys] Disposing ModeAwareService")

        keyHandler.dispose()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ModeAwareService = project.service()
    }
}