package org.example.gitcommitrename

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
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
import kotlin.concurrent.thread

class RenameCommit : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
            ?: throw Exception("Trying to change the name of a commit without an oppen project!")
        val root = GitRepositoryManager.getInstance(project).repositories.firstOrNull()?.root
            ?: throw Exception("Trying to change the name of a commit without the repository!")
        val handler = GitLineHandler(project, root, GitCommand.LOG)
        var previousText = "Failed to load"
        thread {
            previousText = Git.getInstance().runCommand(handler).getOutputOrThrow()
            var startingIndex = previousText.indexOf('\n', previousText.indexOf("Date:"))
            startingIndex = previousText.indexOf('\n', startingIndex + 1)
            previousText = previousText.substring(startingIndex, previousText.indexOf('\n', startingIndex + 1)).trim()
        }.join()
        RenameCommitDialog(previousText, project, root).show()
    }

}

class RenameCommitDialog(val previousCommitText: String, val project: Project, val root: VirtualFile) : DialogWrapper(true) {
    lateinit var textArea:JBTextArea
    init {
        title = "Rename last commit"
        init()
    }
    override fun createCenterPanel(): JComponent {
        val layout = BorderLayout()
        layout.vgap = 10
        val renameDialog = JPanel(layout)
        renameDialog.preferredSize = Dimension(400, 200)
        renameDialog.add(JLabel("Rename commit:"), BorderLayout.PAGE_START)

        textArea = JBTextArea(previousCommitText)
        textArea.border = JBUI.Borders.compound(JBUI.Borders.customLine(JBColor.GRAY, 1), JBUI.Borders.empty(3))
        renameDialog.add(textArea, BorderLayout.CENTER)
        return renameDialog
    }

    override fun doOKAction() {
        val handler = GitLineHandler(project, root, GitCommand.COMMIT)
        handler.addParameters("--amend", "-m " + textArea.text + "")
        thread {
            Git.getInstance().runCommand(handler).getOutputOrThrow()
        }

        super.doOKAction()
    }

}