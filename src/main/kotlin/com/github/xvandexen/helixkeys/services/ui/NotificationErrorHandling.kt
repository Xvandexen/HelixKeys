package com.github.xvandexen.helixkeys.services.utilities

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object NotificationErrorHandling {

  /**
   * Displays an error notification with formatted message
   */
  fun showErrorNotification(project: Project?, title: String, content: String) {
    NotificationGroupManager.getInstance()
      .getNotificationGroup("Helixkeys Notifications")
      .createNotification(title, formatErrorMessage(content), NotificationType.ERROR)
      .notify(project)
  }

  /**
   * Formats error messages for better readability
   */
  private fun formatErrorMessage(message: String): String {
    // Improve formatting for common error patterns
    if (message.contains("ParseError")) {
      val pattern = "ParseError\\(errorDescription=([^,]+), line=(\\d+), cause=(.*)\\)".toRegex()
      val matchResult = pattern.find(message)

      if (matchResult != null) {
        val (description, line, cause) = matchResult.destructured
        return """
                    |<b>Parse Error:</b>
                    |• Description: $description
                    |• Line: $line
                    |• Cause: ${cause.takeIf { it != "null" } ?: "N/A"}
                """.trimMargin()
      }
    }
    return message
  }
}