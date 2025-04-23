package com.github.xvandexen.helixkeys

import com.github.xvandexen.helixkeys.services.commands.CommandExecutor.HelixCommand
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HelixCommandTest : BasePlatformTestCase() {

    fun testActionIdsAreValid() {
        val actionManager = ActionManager.getInstance()


        // Get all registered action IDs for suggestions

        for (command in HelixCommand.values()) {
            val actionId = command.actionId
            if (!actionId.isEmpty()) {
               assertNotNull("$command Action Id: ${command.actionId} is not valid",actionManager.getAction(actionId))

            }

    }
}
    }
