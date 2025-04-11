package com.github.xvandexen.helixkeys.startup

import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig
import com.github.xvandexen.helixkeys.services.configuration.KeyCombo
import com.github.xvandexen.helixkeys.services.functionaltity.ModalKeyManager
import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager
import com.github.xvandexen.helixkeys.services.ui.UiHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer

@Service(Service.Level.PROJECT)
class ModeAwareService(private val project: Project): Disposable {
    private var keyHandler: ModalKeyManager

    init {
        thisLogger().info("[HelixKeys] Initializing ModeAwareService for project: ${project.name}")


        val keybindings: MutableMap<ModeManager.Mode, Map<KeyCombo, KeyBindingConfig.RecKeyBinding>> = KeyBindingConfig().loadConfig()



        thisLogger().info("Pre handler Keybindings = $keybindings")
        keyHandler = ModalKeyManager(project, keybindings)









    }

    override fun dispose() {
        Disposer.dispose(this)

    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ModeAwareService = project.service()
    }
}