package me.him188.ani.app.torrent.libtorrent4j

import me.him188.ani.app.torrent.api.handle.TorrentThread
import me.him188.ani.app.torrent.libtorrent4j.handle.TorrentAddEvent
import me.him188.ani.app.torrent.libtorrent4j.handle.TorrentEvent
import me.him188.ani.app.torrent.libtorrent4j.handle.TorrentSaveResumeDataEvent
import me.him188.ani.utils.logging.info
import org.libtorrent4j.AddTorrentParams
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Libtorrent4jTorrentDownloadSession(
    torrentName: String,
    saveDirectory: File,
    onClose: (DefaultTorrentDownloadSession) -> Unit,
    onDelete: (DefaultTorrentDownloadSession) -> Unit,
    isDebug: Boolean,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : DefaultTorrentDownloadSession(
    torrentName, saveDirectory, onClose, onDelete, isDebug, parentCoroutineContext,
) {
    @TorrentThread
    override fun handleEvent(event: TorrentEvent) {
        when (event) {
            is TorrentAddEvent -> {
                logger.info { "[$torrentName] Received alert: Torrent added" }
                val torrentHandle = event.handle

                // Add trackers
                trackers.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                    torrentHandle.addTracker(it)
                }

                // Initialize [pieces]
                // 注意, 必须在这里初始化获取 pieces, 通过磁力链解析的可能是不准确的
                val contents = torrentHandle.contents
                logger.info { "[$torrentName] Torrent contents: ${contents.files.size} files" }
                actualInfo.complete(ActualTorrentInfo(contents.createPieces(), contents.files))
                logger.info { "[$torrentName] ActualTorrentInfo computed" }
            }

            is TorrentSaveResumeDataEvent -> {
                val data = event.platformData
                check(data is AddTorrentParams)
                logger.info { "[$torrentName] Save resume data, encoding..." }
                val buf = AddTorrentParams.writeResumeDataBuf(data)
                logger.info { "[$torrentName] Save resume data, buf length = ${buf.size}" }
                val file = saveDirectory.resolve(FAST_RESUME_FILENAME)
                file.writeBytes(buf)
                logger.info { "[$torrentName] Resume data saved" }
            }

            else -> {}
        }
        super.handleEvent(event)
    }
}