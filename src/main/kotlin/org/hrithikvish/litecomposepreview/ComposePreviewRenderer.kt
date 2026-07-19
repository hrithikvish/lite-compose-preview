package org.hrithikvish.litecomposepreview

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.util.ExecUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.wm.ToolWindowManager
import org.jetbrains.kotlin.psi.KtNamedFunction

private const val NOTIFICATION_GROUP_ID = "Lite Compose Preview"
private const val TOOL_WINDOW_ID = "MyToolWindow"

object ComposePreviewRenderer {

    fun render(project: Project, function: KtNamedFunction) {
        val virtualFile = function.containingFile.virtualFile ?: return
        val filePath = virtualFile.path
        val composableName = function.name ?: return

        val state = project.service<ComposePreviewState>().apply {
            status = PreviewStatus.LOADING
            this.composableName = composableName
            errorMessage = null
        }

        ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID)?.show()

        object : Task.Backgroundable(
            /* project = */ project,
            /* title = */ "Rendering Compose preview: $composableName",
            /* canBeCancelled = */ true
        ) {
            override fun run(indicator: ProgressIndicator) {
                val androidExecutable = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS("android")
                if (androidExecutable == null) {
                    fail(
                        project = project,
                        state = state,
                        message = "The 'android' CLI was not found on PATH. Install it from " +
                                "https://developer.android.com/tools/agents and make sure Android Studio " +
                                "has Gemini signed in."
                    )
                    return
                }

                val outputFile = FileUtil.createTempFile("lite-compose-preview", ".png", true)
                val pid = ProcessHandle.current().pid()
                val commandLine = GeneralCommandLine(
                    androidExecutable.path,
                    "studio",
                    "render-compose-preview",
                    "--output-image-file=${outputFile.absolutePath}",
                    "--pid=$pid",
                    "--project=${project.name}",
                    filePath,
                    composableName
                )

                try {
                    val output = ExecUtil.execAndGetOutput(commandLine)
                    if (output.exitCode != 0 || !outputFile.exists() || outputFile.length() == 0L) {
                        val message = listOf(output.stderr, output.stdout)
                            .firstOrNull { it.isNotBlank() }
                            ?: "Render failed with exit code ${output.exitCode}."
                        fail(project = project, state = state, message = message)
                        return
                    }

                    val bytes = outputFile.readBytes()
                    ApplicationManager.getApplication().invokeLater {
                        state.apply {
                            imageBytes = bytes
                            this.filePath = filePath
                            status = PreviewStatus.SUCCESS
                        }
                    }
                } catch (ex: ExecutionException) {
                    fail(
                        project = project,
                        state = state,
                        message = "Failed to run the 'android' CLI: ${ex.message}"
                    )
                }
            }
        }.queue()
    }

    private fun fail(project: Project, state: ComposePreviewState, message: String) {
        ApplicationManager.getApplication().invokeLater {
            state.apply {
                status = PreviewStatus.ERROR
                errorMessage = message
            }
        }
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(content = message, type = NotificationType.ERROR)
            .notify(project)
    }
}
