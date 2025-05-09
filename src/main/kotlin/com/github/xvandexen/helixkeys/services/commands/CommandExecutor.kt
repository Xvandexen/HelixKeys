package com.github.xvandexen.helixkeys.services.commands


import com.intellij.ide.DataManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer

/**
 * Service responsible for executing Helix editor commands within the IntelliJ environment.
 * 
 * This class acts as a bridge between Helix-style commands and IntelliJ's action system,
 * allowing users to use Helix keybindings and commands in the IDE.
 *
 * @property project The current project instance
 */
@Service(Service.Level.PROJECT)
class CommandExecutor(val project: Project): Disposable {
  /**
   * Stores the last executed command for potential repetition
   */
  private val lastCommand: HelixCommand? = null

  /**
   * Enumeration of all supported Helix editor commands.
   * 
   * Each command is mapped to a corresponding IntelliJ action ID that will be executed
   * when the command is invoked. Empty action IDs indicate commands that are not yet implemented.
   *
   * @property actionId The IntelliJ action ID associated with this command
   */
  enum class HelixCommand(val actionId : String) {

    //Movement

    MOVE_CHAR_LEFT("EditorLeft"),
    MOVE_CHAR_RIGHT("EditorRight") ,
    MOVE_LINE_UP("EditorUp"),
    MOVE_LINE_DOWN("EditorDown"),
    MOVE_VISUAL_LINE_UP("EditorUp"),
    MOVE_VISUAL_LINE_DOWN("EditorDown"),

    EXTEND_CHAR_LEFT("EditorLeftWithSelection"),
    EXTEND_CHAR_RIGHT("EditorRightWithSelection"),
    EXTEND_LINE_UP("EditorUpWithSelection"),
    EXTEND_LINE_DOWN("EditorDownWithSelection"),
    EXTEND_VISUAL_LINE_UP("EditorUpWithSelection"),
    EXTEND_VISUAL_LINE_DOWN("EditorDownWithSelection"),

    COPY_SELECTION_ON_NEXT_LINE( "HelixKeys.CopySelectionNextLine"),
    COPY_SELECTION_ON_PREV_LINE(  "HelixKeys.CopySelectionPrevLine"),

    MOVE_NEXT_WORD_START(  "HelixKeys.NextWordStart"),
    MOVE_PREV_WORD_START(  "HelixKeys.PrevWordStart"),
    MOVE_NEXT_WORD_END(  "HelixKeys.NextWordEnd"),
    MOVE_PREV_WORD_END("HelixKeys.PrevWordEnd"),

    MOVE_NEXT_LONG_WORD_START( "HelixKeys.LongNextWordStart"),
    MOVE_PREV_LONG_WORD_START( "HelixKeys.LongPrevWordStart"),
    MOVE_NEXT_LONG_WORD_END(""),
    MOVE_PREV_LONG_WORD_END(""),

    //TODO(Subword Implementation)
    MOVE_NEXT_SUB_WORD_START(""),
    MOVE_PREV_SUB_WORD_START(""),
    MOVE_NEXT_SUB_WORD_END( ""),
    MOVE_PREV_SUB_WORD_END(""),

    EXTEND_NEXT_WORD_START(""),
    EXTEND_PREV_WORD_START(""),
    EXTEND_NEXT_WORD_END(""),
    EXTEND_PREV_WORD_END(""),

    EXTEND_NEXT_LONG_WORD_START(""),
    EXTEND_PREV_LONG_WORD_START(""),
    EXTEND_NEXT_LONG_WORD_END(""),
    EXTEND_PREV_LONG_WORD_END(""),

    EXTEND_NEXT_SUB_WORD_START(""),
    EXTEND_PREV_SUB_WORD_START(""),
    EXTEND_NEXT_SUB_WORD_END(""),
    EXTEND_PREV_SUB_WORD_END(""),

    EXTEND_PARENT_NODE_END( ""),
    EXTEND_PARENT_NODE_START( ""),




    FIND_TILL_CHAR(  "HelixKeys.FindTillNextChar"),
    EXTEND_TILL_CHAR( ""),
    FIND_NEXT_CHAR(  "HelixKeys.FindNextChar"),
    EXTEND_NEXT_CHAR( ""),
    TILL_PREV_CHAR(  "HelixKeys.FindTillPrevChar"),
    FIND_PREV_CHAR(  "HelixKeys.FindPrevChar"),
    EXTEND_TILL_PREV_CHAR( ""),
    EXTEND_PREV_CHAR( ""),
    REPEAT_LAST_MOTION(""),
    REPLACE("HelixKeys.Replace"),
    SWITCH_CASE("HelixKeys.SwitchCase"),
    SWITCH_TO_UPPERCASE(  "HelixKeys.SwitchToUpper"),
    SWITCH_TO_LOWERCASE(  "HelixKeys.SwitchToLower"),


    PAGE_UP("EditorPageUp"),
    PAGE_DOWN("EditorPageDown"),
    HALF_PAGE_UP( ""),
    HALF_PAGE_DOWN( ""),
    PAGE_CURSOR_UP( ""),
    PAGE_CURSOR_DOWN( ""),
    PAGE_CURSOR_HALF_UP(""),
    PAGE_CURSOR_HALF_DOWN(""),

    SELECT_ALL("\$SelectAll"),
    SELECT_REGEX(""),
    SPLIT_SELECTION(""),
    SPLIT_SELECTION_ON_NEWLINE(""),
    MERGE_SELECTIONS(""),
    MERGE_CONSECUTIVE_SELECTIONS(""),
    SEARCH(""),
    RSEARCH(""),
    SEARCH_NEXT(""),
    SEARCH_PREV(""),
    EXTEND_SEARCH_NEXT(""),
    EXTEND_SEARCH_PREV(""),
    SEARCH_SELECTION(""),
    SEARCH_SELECTION_DETECT_WORD_BOUNDARIES(""),
    MAKE_SEARCH_WORD_BOUNDED( ""),
    GLOBAL_SEARCH(""),

    EXTEND_LINE( "HelixKeys.ExtendLine"),
    EXTEND_LINE_ABOVE( "HelixKeys.ExtendLineAbove"),
    EXTEND_LINE_BELOW("HelixKeys.ExtendLineBelow"),
    SELECT_LINE_ABOVE( ""),
    SELECT_LINE_BELOW( ""),

    COMMAND_MODE(  ""),
    //TODO(Add own Picker popup)
    FILE_PICKER( "GotoFile"),
    FILE_PICKER_IN_CURRENT_BUFFER_DIRECTORY( ""),
    FILE_PICKER_IN_CURRENT_DIRECTORY("main.toolbar.Project"),
    CODE_ACTION( "ShowIntentionActions"),
    BUFFER_PICKER("RecentFiles"),
    JUMPLIST_PICKER( ""),
    SYMBOL_PICKER( "GotoSymbol"),
    CHANGED_FILE_PICKER( "RecentChangedFiles"),
    SELECT_REFERENCES_TO_SYMBOL_UNDER_CURSOR(""),
    WORKSPACE_SYMBOL_PICKER( ""),
    DIAGNOSTICS_PICKER( ""),
    LAST_PICKER( ""),

    NORMAL_MODE( "HelixKeys.NormalMode"),
    SELECT_MODE( "HelixKeys.SelectMode"),
    //TODO EXIT_SELECT_MODE("HelixKeys.ExitSelectMode"),

    GOTO_DEFINITION( ""),
    GOTO_DECLARATION( ""),

    ADD_NEWLINE_ABOVE( ""),
    ADD_NEW_LINE_BELOW( ""),

    GOTO_TYPE_DEFINITION( "GotoTypeDeclaration"),
    GOTO_IMPLEMENTATION( "GotoImplementation"),
    GOTO_FILE_START( "EditorTextStart"),
    GOTO_FILE_END( "EditorTextEnd"),
    GOTO_FILE( "GotoFile"),
    GOTO_FILE_HSPLIT( "OpenInRightSplit"),
    GOTO_FILE_VSPLIT( "SplitVertically"),//TODO (custom Implemented)
    GOTO_REFERENCES( "FindUsages"),
    GOTO_WINDOW_TOP( "EditorScrollTop"),
    GOTO_WINDOW_CENTER( "EditorScrollToCenter"),
    GOTO_WINDOW_BOTTOM( "EditorScrollBottom"),
    GOTO_LAST_ACCESSED_FILE( "RecentFiles"), //TODO(Custom Implementation)
    GOTO_LAST_MODIFICATION( "JumpToLastChange"), //TODO (Custom)
    GOTO_LINE( "GotoLine"),
    GOTO_LAST_LINE( ""), //TODO Custom implement
    GOTO_FIRST_DIAG( "GotoNextError"), //TODO(Custom Implementaion)
    GOTO_LAST_DIAG( "GotoPreviousError"),
    GOTO_NEXT_CHANGE( "JumpToNextChange"),
    GOTO_FIRST_CHANGE( ""), //TODO Custom
    GOTO_LAST_CHANGE( ""), // TODO Custom
    GOTO_LINE_START( "EditorLineStart"),
    GOTO_LINE_END( "EditorLineEnd"),
    GOTO_NEXT_BUFFER( "NextEditorTab"),
    GOTO_PREVIOUS_BUFFER( "PreviousEditorTab"),
    GOTO_LINE_END_NEWLINE( ""),
    GOTO_FIRST_NONWHITESPACE( ""),
    TRIM_SELECTIONS( ""),
    EXTEND_TO_LINE_START( ""),
    EXTEND_TO_FIRST_NON_WHITESPACE( ""),
    EXTEND_TO_LINE_END( ""),
    EXTEND_TO_LINE_END_NEWLINE( ""),
    SIGNATURE_HELP( ""),
    SMART_TAB( ""),
    INSERT_TAB( ""),
    INSERT_NEWLINE( ""),
    DELETE_CHAR_BACKWARD( ""),
    DELETE_CHAR_FORWARD( ""),
    DELETE_WORD_FORWARD( ""),
    KILL_TO_LINE_START( ""),
    KILL_TO_LINE_END( ""),
    UNDO( "\$Undo"),
    REDO( "\$Redo"),
    EARLIER( ""),
    LATER( ""),
    COMIT_UNDO_CHECKPOINT( ""),
    YANK( ""),
    YANK_TO_CLIPBOARD( "\$Copy"),
    YANK_TO_PRIMARY_CLIPBOARD( ""),
    YANK_JOINED( ""),
    YANK_JOINED_TO_CLIPBOARD( ""),
    YANK_MAIN_SELECTION_TO_CLIPBOARD( ""),
    YANK_JOINED_TO_PRIMARY_CLIPBOARD( ""),
    YANK_MAIN_SELECTION_TO_PRIMARY_CLIPBOARD( ""),

    REPLACE_WITH_YANKED( "\$Paste"),
    REPLACE_SELECTIONS_WITH_CLIPBOARD( ""),
    REPLACE_SELECTIONS_WITH_PRIMARY_CLIPBOARD( ""),
    PASTE_AFTER( ""),
    PASTE_BEFORE( ""),
    PASTE_CLIPBOARD_AFTER( ""),
    PASTE_CLIPBOARD_BEFORE( ""),
    PASTE_PRIMARY_CLIPBOARD_AFTER( ""),
    PASTE_PRIMARY_CLIPBOARD_BEFORE( ""),
    INDENT( ""),
    UNINDENT( ""),
    FORMAT_SELECTIONS( "ReformatCode"),
    JOIN_SELECTIONS( ""),
    JOIN_SELECTIONS_SPACE( ""),
    KEEP_SELECTIONS( ""),
    REMOVE_SELECTIONS( ""),
    ALIGN_SELECTIONS( ""),
    KEEP_PRIMARY_SELECTION( ""),
    REMOVE_PRIMARY_SELECTION( ""),
    COMPLETION( ""),
    HOVER( ""),
    TOGGLE_COMMENTS( "CommentByLineComment"),
    TOGGLE_LINE_COMMENTS( "CommentByLineComment"),
    TOGGLE_BLOCK_COMMENTS( "CommentByBlockComment"),
    ROTATE_SELECTIONS_FORWARD( ""),
    ROTATE_SELECTIONS_BACKWARD( ""),
    ROTATE_SELECTION_CONTENTS_FORWARD( ""),
    ROTATE_SELECTION_CONTENTS_BACKWARD( ""),
    REVERSE_SELECTION_CONTENTS( ""),
    EXPAND_SELECTION( ""),
    SHRINK_SELECTION( ""),
    SELECT_NEXT_SIBLING( ""),
    SELECT_PREV_SIBLING( ""),
    SELECT_ALL_SIBLINGS( ""),
    SELECT_ALL_CHILDREN( ""),
    JUMP_FORWARD(""),
    JUMP_BACKWORD(""),
    SAVE_SELECTION(""),
    JUMP_VIEW_RIGHT( ""),
    JUMP_VIEW_LEFT( ""),
    JUMP_VIEW_DOWN( ""),
    SWAP_VIEW_RIGHT( ""),
    SWAP_VIEW_LEFT( ""),
    SWAP_VIEW_UP( ""),
    SWAP_VIEW_DOWN( ""),
    TRANSPOSE_VIEW( ""),
    ROTATE_VIEW( ""),
    ROTATE_VIEW_REVERSE( ""),
    HSPLIT( ""),
    HSPLIT_NEW( ""),
    VSPLIT( ""),
    VSPLIT_NEW( ""),
    WCLOSE( ""),
    WONLY( ""),
    SELECT_REGISTER( ""),
    INSERT_REGISTER( ""),
    ALIGN_VIEW_MIDDLE( ""),
    ALIGN_VIEW_TOP( ""),
    ALIGN_VIEW_CENTER( ""),
    ALIGN_VIEW_BOTTOM( ""),
    SCROLL_UP( ""),
    SCROLL_DOWN( ""),
    MATCH_BRACKETS( ""),
    SURROUND_ADD( ""),
    SURROUND_REPLACE( ""),
    SURROUND_DELETE( ""),
    SELECT_TEXTOBJECT_AROUND( ""),
    SELECT_TEXTOBJECT_INNER( ""),
    GOTO_NEXT_FUNCTION( ""),
    GOTO_PREV_FUNCTION( ""),
    GOTO_NEXT_CLASS( ""),
    GOTO_PREV_CLASS( ""),
    GOTO_NEXT_PARAMETER( ""),
    GOTO_PREV_PARAMETER( ""),
    GOTO_NEXT_COMMENT( ""),
    GOTO_PREV_COMMENT( ""),
    GOTO_NEXT_TEST( ""),
    GOTO_PREV_TEST( ""),
    GOTO_NEXT_ENTRY( ""),
    GOTO_PREV_ENTRY( ""),
    GOTO_NEXT_PARAGRAPH( ""),
    GOTO_PREV_PARAGRAPH( ""),
    DAP_LAUNCH( ""),
    DAP_RESTART( ""),
    DAP_TOGGLE_BREAKPOINT( ""),
    DAP_CONTINUE( ""),
    DAP_PAUSE( ""),
    DAP_STEP_IN( ""),
    DAP_STEP_OUT( ""),
    DAP_NEXT( ""),
    DAP_VARIABLES( ""),
    DAP_TERMINATE( ""),
    DAP_EDIT_CONDITION( ""),
    DAP_EDIT_LOG( ""),
    DAP_SWITCH_THERAD( ""),
    DAP_SWITCH_STACK_FRAME( ""),
    DAP_ENABLE_EXCEPTIONS( ""),
    DAP_DISABLE_EXCEPTIONS( ""),
    SHELL_PIPE( ""),
    SHELL_PIPE_TO( ""),
    SHELL_INSERT_OUTPUT( ""),
    SHELL_APPEND_OUTPUT( ""),
    SHELL_KEEP_PIPE( ""),
    SUSPEND( ""),
    RENAME_SYMBOL( ""),
    INCREMENT( ""),
    DECREMENT( ""),
    RECORD_MACRO( ""),
    REPLAY_MACRO( ""),
    COMMAND_PALETTE( ""),
    GOTO_WORD( ""),
    EXTEND_TO_WORD( ""),
    GOTO_NEXT_TABSTOP( ""),
    GOTO_PREV_TABSTOP( ""),



    //TODO Order these


    INSERT_MODE("HelixKeys.InsertMode"),//TODO Custom Action
    APPEND_MODE(""),
    INSERT_AT_LINE_START(""),

    INSERT_AT_LINE_END(""),
    OPEN_BELOW(""),
    OPEN_ABOVE(""),
    REPEAT_INSERT(""),




    DELETE_SELECTION(""),
    DELETE_SELECTION_NOYANK(""),
    CHANGE_SELECTION(""),
    CHANGE_SELECTION_NOYANK(""),



    SPILT_SELECTION_ON_NEWLINE(""),
    COLLASPSE_SELECTION(""),

    FLIP_SELECTIONS(""),

    ENSURE_SELECTIONS_FORWARD(""),


    ROTATE_SELECTIONS_CONTENTS_FORWARD(""),


    EXTEND_TO_LINE_BOUNDS(""),
    SHRINK_TO_LINE_BOUNDS(""),

    MOVE_PARENT_NODE_END(""),
    MOVE_PARENT_NODE_START(""),




  }

  /**
   * Executes a Helix command by invoking the corresponding IntelliJ action.
   *
   * @param command The Helix command to execute
   * @param args Optional arguments for the command (currently unused)
   */
  fun executeCommand(command: HelixCommand, args: Any? = null) {
    executeActionById(command.actionId)
  }

  /**
   * Executes an IntelliJ action by its ID.
   *
   * This method retrieves the action from ActionManager, gets the current editor,
   * creates a data context, and performs the action.
   *
   * @param actionId The ID of the IntelliJ action to execute
   */
  fun executeActionById(actionId: String) {
    val action = ActionManager.getInstance().getAction(actionId)
    val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
    val dataContext = DataManager.getInstance().getDataContext(editor.component)
    val event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN, dataContext)

    action.actionPerformed(event)
  }

  /**
   * Disposes of resources used by this service.
   * 
   * This method is called when the service is being shut down.
   */
  override fun dispose() {
    Disposer.dispose(this)
  }

  /**
   * Executes a Helix command multiple times.
   *
   * @param numTimes The number of times to execute the command as a string
   * @param command The Helix command to execute
   * @param args Optional arguments for the command (currently unused)
   */
  fun executeCommandXTimes(numTimes: String, command: HelixCommand, args: Any? = null) {
    for (i in 1..numTimes.toInt()) {
      executeActionById(command.actionId)
    }
  }

  /**
   * Companion object providing access to the CommandExecutor service instance.
   */
  companion object {
    /**
     * Gets the CommandExecutor service instance for the specified project.
     *
     * @param project The project for which to get the CommandExecutor service
     * @return The CommandExecutor service instance
     */
    @JvmStatic
    fun getInstance(project: Project): CommandExecutor = project.service()
  }





}
