package me.him188.ani.app.torrent

import org.libtorrent4j.Priority
import org.libtorrent4j.SessionManager
import org.libtorrent4j.TorrentFlags
import org.libtorrent4j.TorrentInfo
import java.io.File
import kotlin.coroutines.CoroutineContext

public interface TorrentDownloader {
    public val vendor: TorrentLibInfo

    public fun startDownload(torrent: File, coroutineContext: CoroutineContext): TorrentDownloadSession
}

public fun TorrentDownloader(cacheDirectory: File): TorrentDownloader = TorrentDownloaderImpl(cacheDirectory)

public class TorrentLibInfo(
    public val vendor: String,//  "libtorrent"
    public val version: String, // LibTorrent.version()
)

internal class TorrentDownloaderImpl(
    private val cacheDirectory: File,
) : TorrentDownloader {
    override val vendor: TorrentLibInfo = TorrentLibInfo(
        vendor = "libtorrent",
        version = org.libtorrent4j.LibTorrent.version(),
    )

    override fun startDownload(
        torrent: File,
        coroutineContext: CoroutineContext,
    ): TorrentDownloadSession {
        val sessionManager = SessionManager()

        val ti = TorrentInfo(torrent)
        val priorities = Priority.array(Priority.IGNORE, ti.numFiles())
        priorities[0] = Priority.TOP_PRIORITY

        val session = TorrentDownloadSessionImpl(sessionManager, ti, File(ti.files().filePath(1)))
        sessionManager.addListener(session.listener)

        sessionManager.start()

        try {
            sessionManager.download(
                ti,
                cacheDirectory.parentFile,
                null,
                priorities,
                null,
                TorrentFlags.SEQUENTIAL_DOWNLOAD
            )
        } catch (e: Throwable) {
            sessionManager.stop()
            throw e
        }

        return session
    }
}

