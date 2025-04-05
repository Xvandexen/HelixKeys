package com.github.xvandexen.helixkeys.functionaltity

import com.github.xvandexen.helixkeys.services.ModeManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.ide.IdeEventQueue
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.actionSystem.TypedActionHandler
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

class ModeSensitiveHandler(private val modeManager: ModeManager, private val project: Project) : Disposable {
    private val keyListener = object : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {

            val char = e.keyChar
            thisLogger().info("Key Pressed: $char, Mode ${if (modeManager.isSpecialMode()) "Special" else "Normal"}")

            if (char == 's') {
                modeManager.toggleMode()
                e.consume()
                return
            }

            if (modeManager.isSpecialMode()) {
                when (char) {
                    'd' -> {
                        e.consume()
                        val actionManager = ActionManager.getInstance()
                        val action = actionManager.getAction(IdeActions.ACTION_FIND)

                        // Create data context with current editor
                        val editor = EditorFactory.getInstance().allEditors.firstOrNull {
                            it.project == project && it.contentComponent.hasFocus()
                        }

                        if (editor != null) {
                            val dataContext = SimpleDataContext.builder()
                                .add(CommonDataKeys.PROJECT, project)
                                .add(CommonDataKeys.EDITOR, editor)
                                .build()

                            val event = AnActionEvent.createFromDataContext("ModeSensitiveKeyHandler", null, dataContext)
                            action.actionPerformed(event)
                            e.consume()
                        }
                    }
                    // OtherBindings
                    // todo(test multi keypress)
                }
            }
        }
    }


    init {
        // Register the key listener with the IDE frame
        val ideFrame = WindowManager.getInstance().getIdeFrame(project)
        ideFrame?.component?.addKeyListener(keyListener)

        // Register via the key event dispatcher
        IdeEventQueue.getInstance().addDispatcher({ e ->
            if (e is KeyEvent && e.id == KeyEvent.KEY_PRESSED) {
                keyListener.keyPressed(e)
                e.isConsumed
            } else {
                false
            }
        }, this)
    }

    override fun dispose() {
        // The listener will be automatically unregistered when this disposable is disposed
        val ideFrame = WindowManager.getInstance().getIdeFrame(project)
        ideFrame?.component?.removeKeyListener(keyListener)
    }
}