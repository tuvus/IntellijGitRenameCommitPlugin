package org.example.gitcommitrename

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.*
import kotlin.concurrent.thread

/**
 * RenameCommit allows users to modify the last commit message without having to manage staging and unstaging their changes.
 */
class RenameCommit : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
            ?: throw Exception("Trying to change the name of a commit without an open project!")

        // The buttons should only be visible when Git is the current VCS so we shouldn't encounter these errors
        val root = GitRepositoryManager.getInstance(project).repositories.firstOrNull()?.root
            ?: throw Exception("Trying to change the name of a commit without the repository!")

        // We should get the current message of the last commit to pre-fill the text input later
        val handler = GitLineHandler(project, root, GitCommand.LOG)
        var previousText = "Failed to load"
        var commitExists = true

        // Git commands are not allowed to be run on the Event Dispatch Thread so we need to create a new thread
        thread {
            val output = Git.getInstance().runCommand(handler)
            // Check if the Git repository has no commits, in this case fail silently
            if (output.errorOutput.isNotEmpty()) {
                commitExists = false
                return@thread
            }

            // Extract the Git message from Git log
            previousText = output.getOutputOrThrow()
            var startingIndex = previousText.indexOf('\n', previousText.indexOf("Date:"))
            startingIndex = previousText.indexOf('\n', startingIndex + 1)
            var endingIndex = previousText.indexOf('\n', startingIndex + 1)
            // If there is only one commit in the history we probably won't find another newline
            if (endingIndex == -1) endingIndex = previousText.length - 1
            previousText = previousText.substring(startingIndex, endingIndex).trim()


            // Adding the RenameCommitDialog must be done on the EDT thread
            // so we must wait for the Git commands to complete
            // We also don't want the previous EDT thread to wait for the Git commands
            // since that defeats the purpose of keeping the EDT thread for UI
            CoroutineScope(Dispatchers.EDT).launch {
                if (commitExists) RenameCommitDialog(previousText, project, root).show()
            }
        }
    }
}

/**
 * RenameCommitDialog is the popup that asks the user for a new commit message to change the last commit message to.
 */
class RenameCommitDialog(val previousCommitText: String, val project: Project, val root: VirtualFile) :
    DialogWrapper(true) {
    lateinit var textArea: JBTextArea

    init {
        title = "Rename last commit"
        init()
    }

    /**
     * Display the rename dialog+
     */
    override fun createCenterPanel(): JComponent {
        val layout = BorderLayout()
        layout.vgap = 10
        val renameDialog = JPanel(layout)
        renameDialog.preferredSize = Dimension(400, 100)
        renameDialog.add(JLabel("Rename commit:"), BorderLayout.PAGE_START)

        textArea = JBTextArea(previousCommitText)
        // Create a line around the text along with some padding
        textArea.border = JBUI.Borders.compound(
            JBUI.Borders.customLine(JBColor.GRAY, 1), JBUI.Borders.empty(3)
        )
        renameDialog.add(textArea, BorderLayout.CENTER)
        return renameDialog
    }

    /**
     * Changes the commit message when the user presses OK on the dialog
     */
    override fun doOKAction() {
        // Git Commands must be done in another non EDT-thread
        thread {
            // First we need to stash all changes so that they aren't added to the commit
            val stashHandler = GitLineHandler(project, root, GitCommand.STASH)
            Git.getInstance().runCommand(stashHandler).getOutputOrThrow()

            // Next we need to amend the commit to have the new message
            val amendHandler = GitLineHandler(project, root, GitCommand.COMMIT)
            amendHandler.addParameters("--amend", "-m " + textArea.text)

            // Finally un-stash the changes again so that we are where we left off
            Git.getInstance().runCommand(amendHandler).getOutputOrThrow()
            val applyStashHandler = GitLineHandler(project, root, GitCommand.STASH)
            applyStashHandler.addParameters("apply")
            Git.getInstance().runCommand(applyStashHandler).getOutputOrThrow()
        }

        super.doOKAction()
    }

}