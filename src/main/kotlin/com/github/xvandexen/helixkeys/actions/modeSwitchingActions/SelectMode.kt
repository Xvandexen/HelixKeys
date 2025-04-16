package com.github.xvandexen.helixkeys.actions.modeSwitchingActions

import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class SelectMode: AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
      ModeManager
        .getInstance(e.project!!)
        .toggleMode(ModeManager.Mode.SELECT)

    }


  }
