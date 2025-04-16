package com.github.xvandexen.helixkeys.startup

import com.github.xvandexen.helixkeys.handlers.CustomTypingHandler
import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig
import com.github.xvandexen.helixkeys.services.configuration.KeyCombo
import com.github.xvandexen.helixkeys.services.functionaltity.ModalKeyManager
import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class ModeAwareService(project: Project): Disposable {
    private var keyHandler: ModalKeyManager

    init {
        thisLogger().info("[HelixKeys] Initializing ModeAwareService for project: ${project.name}")

        val typedAction = TypedAction.getInstance()
        val originalHandler = typedAction.rawHandler
        typedAction.setupRawHandler(CustomTypingHandler(originalHandler))

        keyHandler = ModalKeyManager(project)

        // Register editor factory listener to set block cursor
        val editorFactory = EditorFactory.getInstance()
        editorFactory.addEditorFactoryListener(object : EditorFactoryListener {
            override fun editorCreated(event: EditorFactoryEvent) {
                val editor = event.editor
                // Set block cursor
                editor.settings.isBlockCursor = true
            }
        }, this)

        // Also apply to existing editors
        for (editor in EditorFactory.getInstance().allEditors) {
            editor.settings.isBlockCursor = true
        }
    }

    override fun dispose() {
        keyHandler.dispose()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ModeAwareService = project.service()
    }
}