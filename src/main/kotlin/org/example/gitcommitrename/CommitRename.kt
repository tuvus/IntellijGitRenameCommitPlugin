package org.example.gitcommitrename

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class RenameCommit : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        RenameCommitDialog().show()
    }

}

class RenameCommitDialog() : DialogWrapper(true) {
    init {
        title = "Rename last commit"
        init()
    }
    override fun createCenterPanel(): JComponent {
        val renameDialog = JPanel(BorderLayout())
        val label = JBTextField("last commit name")
//        label.preferredSize = Dimension(100, 100)
        renameDialog.add(label)
        return renameDialog
    }

    override fun doOKAction() {
        super.doOKAction()
    }

}