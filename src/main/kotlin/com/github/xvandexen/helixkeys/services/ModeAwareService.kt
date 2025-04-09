package com.github.xvandexen.helixkeys.services

import com.github.xvandexen.helixkeys.commands.CommandExecutor
import com.github.xvandexen.helixkeys.commands.CommandExecutor.HelixCommand.*
import com.github.xvandexen.helixkeys.configuration.KeyBindingConfig
import com.github.xvandexen.helixkeys.functionaltity.ModalKeyManager
import com.github.xvandexen.helixkeys.functionaltity.ModeManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class ModeAwareService(private val project: Project): Disposable {
    private var modeManager: ModeManager
    private var keyHandler: ModalKeyManager

    init {
        thisLogger().info("[HelixKeys] Initializing ModeAwareService for project: ${project.name}")


        val commandExecutor = CommandExecutor()
        commandExecutor.executeCommand(ENTER_NORMAL_MODE)



        val keybindings: MutableMap<ModeManager.Mode, Map<Set<Int>, KeyBindingConfig.RecKeyBinding>> = KeyBindingConfig().loadConfig()


        modeManager = ModeManager(project)

        keyHandler = ModalKeyManager(modeManager,project, keybindings)




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