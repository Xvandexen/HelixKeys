package com.github.xvandexen.helixkeys.actions.findActions

import com.github.xvandexen.helixkeys.actions.findActions.FindActions.setupCharacterFinder
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action that finds the next occurrence of a specified character.
 * 
 * This action implements the Helix editor's "f" command, which moves the cursor
 * to the next occurrence of a character that the user specifies.
 */
class FindNextChar : AnAction() {
  /**
   * Performs the action of setting up a character finder for finding the next character.
   * 
   * This method delegates to the setupCharacterFinder utility method with the NEXT find type.
   *
   * @param e The action event containing context information
   */
  override fun actionPerformed(e : AnActionEvent) {
    val executor = CommandExecutor.getInstance(e.project!!)
    setupCharacterFinder(executor, FindActions.FindType.NEXT)
  }
}
