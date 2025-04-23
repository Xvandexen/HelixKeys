package com.github.xvandexen.helixkeys.actions.findActions

import com.github.xvandexen.helixkeys.actions.findActions.FindActions.setupCharacterFinder
import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action that finds the previous occurrence of a specified character.
 * 
 * This action implements the Helix editor's "F" command, which moves the cursor
 * to the previous occurrence of a character that the user specifies.
 */
class FindPrevChar: AnAction() {
  /**
   * Performs the action of setting up a character finder for finding the previous character.
   * 
   * This method delegates to the setupCharacterFinder utility method with the PREV find type.
   *
   * @param p0 The action event containing context information
   */
  override fun actionPerformed(p0: AnActionEvent) {
    val executor = CommandExecutor.getInstance(p0.project!!)
    setupCharacterFinder(executor, FindActions.FindType.PREV)
  }
}
