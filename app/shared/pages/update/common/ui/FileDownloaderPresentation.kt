package me.him188.ani.app.update.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.update.FileDownloader

/**
 * 将 [FileDownloader] 状态收集为 Compose [State]
 */
@Stable
class FileDownloaderPresentation(
    downloader: FileDownloader,
    override val backgroundScope: CoroutineScope,
) : HasBackgroundScope {
    val progress by downloader.progress.produceState(0f)
    val state by downloader.state.produceState()
}
