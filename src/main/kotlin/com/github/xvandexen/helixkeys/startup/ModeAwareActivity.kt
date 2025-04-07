package com.github.xvandexen.helixkeys.startup

import com.github.xvandexen.helixkeys.services.ModeAwareService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class ModeAwareActivity : ProjectActivity {
    override suspend fun execute(project: Project) {

        ModeAwareService.getInstance(project)
    }
}