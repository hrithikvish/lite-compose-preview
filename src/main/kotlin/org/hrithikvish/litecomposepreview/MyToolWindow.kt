package org.hrithikvish.litecomposepreview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.ui.component.Text
import java.io.ByteArrayInputStream

class MyToolWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.addComposeTab("Compose Preview", focusOnClickInside = true) {
            MyToolWindowContent(project)
        }
    }
}

@Composable
private fun MyToolWindowContent(project: Project) {
    val state = remember { project.service<ComposePreviewState>() }

    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (state.status) {
            PreviewStatus.IDLE ->
                Text("Click the run icon next to a @Preview composable to render it here.")

            PreviewStatus.LOADING ->
                Text("Rendering ${state.composableName}…")

            PreviewStatus.ERROR ->
                Text("Failed to render ${state.composableName}: ${state.errorMessage}")

            PreviewStatus.SUCCESS -> {
                Text(state.composableName.orEmpty())
                val bytes = state.imageBytes
                if (bytes != null) {
                    val bitmap = remember(bytes) { loadImageBitmap(ByteArrayInputStream(bytes)) }
                    Image(
                        bitmap = bitmap,
                        contentDescription = state.composableName,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
