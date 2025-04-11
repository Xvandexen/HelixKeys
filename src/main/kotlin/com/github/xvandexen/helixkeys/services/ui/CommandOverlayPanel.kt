package com.github.xvandexen.helixkeys.services.ui

import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.MouseEvent
import javax.swing.JLayeredPane
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import com.intellij.openapi.keymap.KeymapUtil
import javax.swing.event.MouseInputAdapter

/**
 * CommandOverlayPanel that displays available key bindings in the editor
 * without interfering with editor interaction
 */
class CommandOverlayPanel(private val project: Project) : JBPanel<CommandOverlayPanel>(BorderLayout()), Disposable {

  private var isVisible = false

  init {
    // Make panel semi-transparent with a border
    background = JBColor(Color(40, 40, 40, 200), Color(60, 60, 60, 200))
    border = IdeBorderFactory.createRoundedBorder(8)
    preferredSize = JBUI.size(250, 150)
    isOpaque = true

    // Make the panel non-interactive by having it ignore all mouse events
    addMouseListener(PassthroughMouseListener())
    addMouseMotionListener(PassthroughMouseListener())

    // Initially hidden
    isVisible = false
  }

  /**
   * A mouse listener that passes all events through to components below
   */
  private inner class PassthroughMouseListener : MouseInputAdapter() {
    override fun mouseClicked(e: MouseEvent) = e.consume()
    override fun mousePressed(e: MouseEvent) = e.consume()
    override fun mouseReleased(e: MouseEvent) = e.consume()
    override fun mouseEntered(e: MouseEvent) = e.consume()
    override fun mouseExited(e: MouseEvent) = e.consume()
    override fun mouseDragged(e: MouseEvent) = e.consume()
    override fun mouseMoved(e: MouseEvent) = e.consume()
  }

  /**
   * Override to ensure mouse events are ignored and passed through
   */
  override fun contains(x: Int, y: Int): Boolean {
    // Always return false to let mouse events pass through
    return false
  }

  /**
   * Updates the panel with new bindings and shows it
   */
  fun showBindings(subBindings: Map<Char, KeyBindingConfig.RecKeyBinding>) {
    removeAll()

    // Panel title
    val titleLabel = JBLabel("Available Commands")
    titleLabel.foreground = JBColor.WHITE
    titleLabel.font = JBUI.Fonts.label().biggerOn(2F).asBold()
    titleLabel.border = JBUI.Borders.empty(5, 10, 5, 10)
    add(titleLabel, BorderLayout.NORTH)

    // Create panel for bindings
    val bindingsPanel = JBPanel<JBPanel<*>>(GridLayout(0, 1, 5, 0))
    bindingsPanel.isOpaque = false
    bindingsPanel.border = JBUI.Borders.empty(5)

    // Make all child components non-interactive too
    bindingsPanel.addMouseListener(PassthroughMouseListener())

    // Add each binding to the panel
    for ((key, binding) in subBindings) {


      val bindingPanel = JBPanel<JBPanel<*>>(BorderLayout(5, 0))
      bindingPanel.isOpaque = false
      bindingPanel.addMouseListener(PassthroughMouseListener())

      val keyLabel = JBLabel("$key:     ")
      keyLabel.foreground = JBColor.BLUE
      keyLabel.font = JBUI.Fonts.label().asBold()
      keyLabel.addMouseListener(PassthroughMouseListener())
      bindingPanel.add(keyLabel, BorderLayout.WEST)

      val commandLabel = JBLabel(binding.command.toString())
      commandLabel.foreground = JBColor.RED
      commandLabel.addMouseListener(PassthroughMouseListener())
      bindingPanel.add(commandLabel, BorderLayout.CENTER)

      bindingsPanel.add(bindingPanel)
    }

    add(bindingsPanel, BorderLayout.CENTER)

    // Make visible
    isVisible = true

    // Update in editor
    updatePosition()

    // Ensure visible
    revalidate()
    repaint()
  }

  /**
   * Hides the panel
   */
  fun hidePanel() {
    if (isVisible) {
      isVisible = false

      // Check if still attached to any parent
      val parent = parent
      if (parent != null) {
        parent.remove(this)
        parent.revalidate()
        parent.repaint()
      }
    }
  }

  /**
   * Updates the position of the panel in the current editor
   */
  private fun updatePosition() {
    // Get current editor
    val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return

    // Try to add to editor's layered pane
    SwingUtilities.invokeLater {
      attachToEditor(editor)
    }
  }

  /**
   * Attaches the panel to the editor's layered pane
   */
  private fun attachToEditor(editor: Editor) {
    if (!isVisible) return

    // Get component and layered pane
    val editorComponent = editor.component
    val layeredPane = findLayeredPane(editorComponent)

    if (layeredPane != null) {
      // Remove from current parent if attached
      val parent = parent
      if (parent != null) {
        parent.remove(this)
      }

      // Add to layered pane, above other components
      layeredPane.add(this, JLayeredPane.POPUP_LAYER + 1)

      // Position in bottom right with margin
      val bounds = layeredPane.bounds
      val x = bounds.width - preferredSize.width - 20
      val y = bounds.height - preferredSize.height - 20

      setBounds(x, y, preferredSize.width, preferredSize.height)

      layeredPane.revalidate()
      layeredPane.repaint()
    }
  }

  /**
   * Finds a JLayeredPane ancestor for the component
   */
  private fun findLayeredPane(component: Component): JLayeredPane? {
    var current: Component? = component

    while (current != null) {
      if (current is JLayeredPane) {
        return current
      }
      current = current.parent
    }

    return null
  }

  override fun dispose() {
    hidePanel()
  }
}