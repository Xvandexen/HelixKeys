package com.github.xvandexen.helixkeys.actions.findActions

import com.github.xvandexen.helixkeys.actions.findActions.FindActions.FindType
import com.github.xvandexen.helixkeys.actions.findActions.FindActions.setupCharacterFinder
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class FindTIllNextChar : AnAction(){
  override fun actionPerformed(e: AnActionEvent) {
    val executor = CommandExecutor.getInstance(e.project!!)
    setupCharacterFinder(executor, FindType.TILL_NEXT)
  }
}