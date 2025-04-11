package com.github.xvandexen.helixkeys.services.ui



import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig
import com.github.xvandexen.helixkeys.services.functionaltity.ModeManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Disposer
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.jetbrains.rd.generator.nova.PredefinedType
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.KeyStroke
import javax.swing.SwingUtilities


@Service(Service.Level.PROJECT)
class UiHandler(val project: Project): Disposable{

  val statusService = HelixKeysStatusService.getInstance(project)
  private val commandOverlay = CommandOverlayPanel(project)


  fun displayMenu(subBindings: Map<Char, KeyBindingConfig.RecKeyBinding>) {
    // Use the overlay panel instead of popup
    commandOverlay.showBindings(subBindings)
  }



  fun closeMenu() {
    // Hide the overlay panel
    commandOverlay.hidePanel()
  }

  fun switchMode(mode: ModeManager.Mode){

    when(mode){
      ModeManager.Mode.NORMAL-> statusService.updateText("NORMAL")
      ModeManager.Mode.INSERT -> statusService.updateText("INSERT")
    }
  }





  override fun dispose() {
    Disposer.dispose(this)
  }


  companion object {
    @JvmStatic
    fun getInstance(project: Project): UiHandler = project.service()
  }

}
