package com.github.xvandexen.helixkeys.startup

import com.github.xvandexen.helixkeys.functionaltity.KeybindingConfig
import com.github.xvandexen.helixkeys.services.ModeAwareService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.startup.StartupActivity

class ModeAwareActivity : ProjectActivity {
    override suspend fun execute(project: Project) {


        ModeAwareService.getInstance(project)
    }
}