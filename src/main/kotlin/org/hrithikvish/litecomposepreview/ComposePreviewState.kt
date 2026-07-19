package org.hrithikvish.litecomposepreview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.intellij.openapi.components.Service

enum class PreviewStatus { IDLE, LOADING, SUCCESS, ERROR }

@Service(Service.Level.PROJECT)
class ComposePreviewState {
    var status by mutableStateOf(PreviewStatus.IDLE)
    var composableName by mutableStateOf<String?>(null)
    var filePath by mutableStateOf<String?>(null)
    var imageBytes by mutableStateOf<ByteArray?>(null)
    var errorMessage by mutableStateOf<String?>(null)
}
