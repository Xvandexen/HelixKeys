package com.github.xvandexen.helixkeys.actions.findActions

import com.github.xvandexen.helixkeys.actions.findActions.FindActions.setupCharacterFinder
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class FindPrevChar: AnAction() {
  override fun actionPerformed(p0: AnActionEvent) {
    val executor = CommandExecutor.getInstance(p0.project!!)
    setupCharacterFinder(executor, FindActions.FindType.PREV)
  }


}