package me.him188.ani.app.torrent.anitorrent.session

import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.torrent.api.files.DownloadStats

class MutableDownloadStats : DownloadStats() {
    override val totalSize: MutableStateFlow<Long> = MutableStateFlow(0)
    override val uploadRate: MutableStateFlow<Long> = MutableStateFlow(0)
    override val downloadRate: MutableStateFlow<Long> = MutableStateFlow(0)
    override val progress: MutableStateFlow<Float> = MutableStateFlow(0f)
    override val downloadedBytes: MutableStateFlow<Long> = MutableStateFlow(0)
    val uploadedBytes: MutableStateFlow<Long> = MutableStateFlow(0)
    override val isFinished: MutableStateFlow<Boolean> = MutableStateFlow(false)
}