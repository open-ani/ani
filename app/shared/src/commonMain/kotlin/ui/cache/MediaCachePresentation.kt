package me.him188.ani.app.ui.cache

import androidx.compose.runtime.Stable
import me.him188.ani.datasources.api.topic.FileSize

@Stable
fun renderFileSize(size: FileSize): String {
    if (size == FileSize.Unspecified) {
        return ""
    }
    return "$size"
}

@Stable
fun renderSpeed(speed: FileSize): String {
    if (speed == FileSize.Unspecified) {
        return ""
    }
    return "$speed/s"
}