package com.github.xvandexen.helixkeys.actions.findActions

import com.github.xvandexen.helixkeys.actions.findActions.FindActions.setupCharacterFinder
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class FindNextChar :AnAction() {
  override fun actionPerformed(e : AnActionEvent) {
    val executor = CommandExecutor.getInstance(e.project!!)
    setupCharacterFinder(executor, FindActions.FindType.NEXT)
  }
}