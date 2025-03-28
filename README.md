## Rename your Git commits easier

This Intellij plugin helps you rename your git commits by adding a convenient toolbar item under Git.
Clicking it opens up a dialog to provide the new name of the commit.

The plugin renames the last Git commit by stashing the current changes, amending the commit with the new message and unstashing the changes again.
By default, the last commit message will be populated into the dialog so the user can quickly make minor spelling corrections.
The plugin runs the Git commands on a different thread so that the EDT or UI thread won't be blocked.

The major source files can be found at:
* [Kotlin](https://github.com/tuvus/IntellijGitRenameCommitPlugin/blob/main/src/main/kotlin/org/example/gitcommitrename/CommitRename.kt)
* [Plugin XML](https://github.com/tuvus/IntellijGitRenameCommitPlugin/blob/main/src/main/resources/META-INF/plugin.xml)

<img alt="Commit toolbar item" height="50%" src="Gitcommitrename.png" width="50%"/>

<img alt="Commit dialog" height="50%" src="Gitcommitrename2.png" width="50%"/>
<img alt="Commit dialog with new UI" height="40%" src="Gitcommitrename3.png" width="40%"/>
