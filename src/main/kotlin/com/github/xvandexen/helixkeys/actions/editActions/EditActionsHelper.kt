package com.github.xvandexen.helixkeys.actions.editActions

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor
import com.github.xvandexen.helixkeys.services.utilities.NotificationErrorHandling
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.fileEditor.FileEditorManager

object EditActionsHelper {




   fun executeUsingTypedChar(
    executor: CommandExecutor,
    hintText: String,
    thingToDo: (Char) -> Unit
  ){
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