package com.github.xvandexen.helixkeys.services.ui.commandoverlay

import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig
import com.github.xvandexen.helixkeys.services.configuration.KeyBindingConfig.toFormattedString
import com.github.xvandexen.helixkeys.services.configuration.KeyCombo
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.MouseEvent
import javax.swing.JLayeredPane
import javax.swing.SwingUtilities
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.scale.JBUIScale
import javax.swing.event.MouseInputAdapter

/**
 * UI component that displays available key bindings in the editor.
 * 
 * This panel shows a semi-transparent overlay in the editor with available
 * key bindings and their corresponding commands. It's designed to be non-intrusive,
 * passing all mouse events through to the editor underneath.
 *
 * @property project The current project instance
 */
class CommandOverlayPanel(private val project: Project) : JBPanel<CommandOverlayPanel>(BorderLayout()), Disposable {

  /**
   * Flag indicating whether the panel is currently visible.
   */
  private var isVisible = false

  /**
   * Logger instance for this class.
   */
  private val LOG = Logger.getInstance(CommandOverlayPanel::class.java)

  /**
   * Reference to the current layered pane that contains this panel.
   */
  private var currentLayeredPane: JLayeredPane? = null

  init {
    // Make panel semi-transparent with a border
    background = JBColor(Color(40, 40, 40, 200), Color(60, 60, 60, 200))
    border = IdeBorderFactory.createRoundedBorder(8)
    preferredSize = JBUI.size(250, 150)
    isOpaque = true

    // Panel should be invisible until explicitly shown
    isVisible = false
    setVisible(false)
  }

  /**
   * A mouse listener that passes all events through to components below.
   * 
   * This listener consumes all mouse events without processing them,
   * effectively making the panel transparent to mouse interactions.
   * This allows the user to interact with the editor underneath the overlay.
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
   * Overrides the contains method to make the panel transparent to mouse events.
   * 
   * By always returning false, this method ensures that the panel never
   * captures mouse events, allowing them to pass through to components below.
   *
   * @param x The x coordinate
   * @param y The y coordinate
   * @return Always false to pass events through
   */
  override fun contains(x: Int, y: Int): Boolean {
    return false // Always return false to pass events through
  }

  /**
   * Updates the panel with new bindings and shows it.
   * 
   * This method populates the panel with the provided key bindings,
   * attaches it to the current editor if needed, and makes it visible.
   * It ensures the operation is performed on the EDT.
   *
   * @param subBindings The map of key combinations to their corresponding bindings
   */
  fun showBindings(subBindings: Map<KeyCombo, KeyBindingConfig.RecKeyBinding>) {
    if (ApplicationManager.getApplication().isDispatchThread) {
      updateBindingsPanel(subBindings)
    } else {
      SwingUtilities.invokeLater { updateBindingsPanel(subBindings) }
    }
  }

  private fun updateBindingsPanel(subBindings: Map<KeyCombo, KeyBindingConfig.RecKeyBinding>) {
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

    // Add each binding to the panel
    for ((key, binding) in subBindings) {
      val bindingPanel = JBPanel<JBPanel<*>>(BorderLayout(5, 0))
      bindingPanel.isOpaque = false

      val keyLabel = JBLabel("${key.toFormattedString()}:     ")
      keyLabel.foreground = JBColor.BLUE
      keyLabel.font = JBUI.Fonts.label().asBold()
      bindingPanel.add(keyLabel, BorderLayout.WEST)

      val commandLabel = JBLabel(binding.command.toString())
      commandLabel.foreground = JBColor.RED
      bindingPanel.add(commandLabel, BorderLayout.CENTER)

      bindingsPanel.add(bindingPanel)
    }

    add(bindingsPanel, BorderLayout.CENTER)

    // Attach to editor if not yet attached
    val editor = FileEditorManager.getInstance(project).selectedTextEditor
    if (editor != null) {
      attachToEditor(editor)
    } else {
      LOG.warn("Cannot show command overlay: no text editor selected")
      return
    }

    // Make visible and update position
    isVisible = true
    setVisible(true)
    updatePosition()

    // Ensure visible
    revalidate()
    repaint()
  }

  /**
   * Hides the panel and removes it from the layered pane.
   * 
   * This method makes the panel invisible and removes it from its parent
   * layered pane. It ensures the operation is performed on the EDT.
   */
  fun hidePanel() {
    if (ApplicationManager.getApplication().isDispatchThread) {
      performHide()
    } else {
      SwingUtilities.invokeLater { performHide() }
    }
  }

  /**
   * Performs the actual hiding of the panel.
   * 
   * This method sets the panel to invisible, removes it from its parent
   * layered pane, and clears the reference to the current layered pane.
   */
  private fun performHide() {
    isVisible = false
    setVisible(false)

    // Remove from layered pane if attached
    val parent = parent
    if (parent is JLayeredPane) {
      parent.remove(this)
      parent.revalidate()
      parent.repaint()
      currentLayeredPane = null
    }
  }

  /**
   * Updates the position of the panel in the current editor.
   * 
   * This method positions the panel in the bottom right corner of the
   * visible area of the editor, with some padding.
   */
  private fun updatePosition() {
    val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
    val visibleRect = editor.scrollingModel.visibleArea

    // Position in bottom right with some padding
    val padding = JBUIScale.scale(20)
    val x = visibleRect.x + visibleRect.width - width - padding
    val y = visibleRect.y + visibleRect.height - height - padding

    setBounds(x, y, width, height)
  }

  /**
   * Attaches the panel to the editor's layered pane.
   * 
   * This method finds the layered pane associated with the editor,
   * removes the panel from its current parent if needed, and adds it
   * to the new layered pane in the popup layer.
   *
   * @param editor The editor to attach the panel to
   */
  private fun attachToEditor(editor: Editor) {
    val layeredPane = findLayeredPane(editor.component)
    if (layeredPane != null) {
      // Only attach if not already attached to this pane
      if (currentLayeredPane != layeredPane) {
        // Remove from current parent if needed
        val parent = parent
        if (parent is JLayeredPane) {
          parent.remove(this)
        }

        // Add to new layered pane
        layeredPane.add(this, JLayeredPane.POPUP_LAYER)
        currentLayeredPane = layeredPane

        // Make sure size is calculated
        setSize(preferredSize)
      }
    } else {
      LOG.warn("Cannot attach command overlay: no layered pane found in editor")
    }
  }

  /**
   * Finds a JLayeredPane ancestor for the component.
   * 
   * This method recursively searches up the component hierarchy
   * to find a JLayeredPane ancestor.
   *
   * @param component The component to find a layered pane for
   * @return The JLayeredPane ancestor, or null if none is found
   */
  private fun findLayeredPane(component: Component): JLayeredPane? {
    if (component is JLayeredPane) {
      return component
    }

    val parent = component.parent ?: return null
    return findLayeredPane(parent)
  }

  /**
   * Disposes of resources used by this panel.
   * 
   * This method is called when the panel is being shut down.
   * It hides the panel and removes it from its parent.
   */
  override fun dispose() {
    hidePanel()
  }
}
