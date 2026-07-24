package org.hrithikvish.litecomposepreview

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
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
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.ui.component.Text
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

private const val NOTIFICATION_GROUP_ID = "Lite Compose Preview"

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
                    ContextMenuArea(
                        items = { listOf(ContextMenuItem("Copy Image") { copyImageToClipboard(project, bytes) }) }
                    ) {
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
}

private fun copyImageToClipboard(project: Project, bytes: ByteArray) {
    val image = ImageIO.read(ByteArrayInputStream(bytes)) ?: return
    val transferable = object : Transferable {
        override fun getTransferDataFlavors() = arrayOf(DataFlavor.imageFlavor)
        override fun isDataFlavorSupported(flavor: DataFlavor) = flavor == DataFlavor.imageFlavor
        override fun getTransferData(flavor: DataFlavor): Any {
            if (!isDataFlavorSupported(flavor)) throw UnsupportedFlavorException(flavor)
            return image
        }
    }
    Toolkit.getDefaultToolkit().systemClipboard.setContents(transferable, null)

    NotificationGroupManager.getInstance()
        .getNotificationGroup(NOTIFICATION_GROUP_ID)
        .createNotification(content = "Preview image copied to clipboard", type = NotificationType.INFORMATION)
        .notify(project)
}
