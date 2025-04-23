package com.github.xvandexen.helixkeys.services.utilities

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

/**
 * Utility object for displaying error notifications in the IDE.
 * 
 * This object provides methods to show formatted error notifications
 * and to format error messages for better readability.
 */
object NotificationErrorHandling {

  /**
   * Displays an error notification with a formatted message.
   * 
   * This method creates and shows an error notification in the IDE,
   * with the specified title and content. The content is formatted
   * for better readability using the formatErrorMessage method.
   *
   * @param project The project context for the notification, or null for application-level notifications
   * @param title The title of the notification
   * @param content The content of the notification, which will be formatted
   */
  fun showErrorNotification(project: Project?, title: String, content: String) {
    NotificationGroupManager.getInstance()
      .getNotificationGroup("Helixkeys Notifications")
      .createNotification(title, formatErrorMessage(content), NotificationType.ERROR)
      .notify(project)
  }

  /**
   * Formats error messages for better readability.
   * 
   * This method improves the formatting of error messages, especially
   * for parse errors, by extracting and structuring the relevant information.
   *
   * @param message The error message to format
   * @return The formatted error message
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
