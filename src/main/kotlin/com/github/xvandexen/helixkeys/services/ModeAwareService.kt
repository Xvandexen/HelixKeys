package com.github.xvandexen.helixkeys.services

import com.github.xvandexen.helixkeys.configuration.KeyBindingConfig
import com.github.xvandexen.helixkeys.functionaltity.ModalKeyManager
import com.github.xvandexen.helixkeys.functionaltity.ModeManager
import com.github.xvandexen.helixkeys.ui.UiHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class ModeAwareService(private val project: Project): Disposable {
    private var keyHandler: ModalKeyManager

    init {
        thisLogger().info("[HelixKeys] Initializing ModeAwareService for project: ${project.name}")
        UiHandler.init(project)

        val keybindings: MutableMap<ModeManager.Mode, Map<Set<Int>, KeyBindingConfig.RecKeyBinding>> = KeyBindingConfig().loadConfig()



        thisLogger().info("Pre handler Keybindings = $keybindings")
        keyHandler = ModalKeyManager(project, keybindings)




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