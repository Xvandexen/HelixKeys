package com.github.xvandexen.helixkeys.actions.editActions

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.github.xvandexen.helixkeys.services.utilities.NotificationErrorHandling
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.fileEditor.FileEditorManager

/**
 * Helper object providing utility methods for edit actions.
 * 
 * This object contains methods that are commonly used by various edit actions
 * in the HelixKeys plugin.
 */
object EditActionsHelper {

  /**
   * Executes an action using the next character typed by the user.
   * 
   * This method shows a hint to the user, then sets up a temporary handler to capture
   * the next character typed. Once the character is typed, it invokes the provided function
   * with that character and restores the original handler.
   *
   * @param executor The CommandExecutor instance
   * @param hintText The hint text to display to the user
   * @param thingToDo A function that takes the typed character and performs an action
   */
  fun executeUsingTypedChar(
    executor: CommandExecutor,
    hintText: String,
    thingToDo: (Char) -> Unit
  ) {
    val editor = FileEditorManager.getInstance(executor.project).selectedTextEditor ?: return
    val originalHandler = TypedAction.getInstance().rawHandler
    HintManager.getInstance().showInformationHint(editor, hintText)

    // Set up a temporary handler to capture the next character
    TypedAction.getInstance().setupRawHandler { _, typedChar, _ ->
      thisLogger().info("Typed char: $typedChar")
      thingToDo.invoke(typedChar)
      TypedAction.getInstance().setupRawHandler(originalHandler)
    }
  }
}
