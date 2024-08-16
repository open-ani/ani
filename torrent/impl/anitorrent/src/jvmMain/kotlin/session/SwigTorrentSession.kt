package me.him188.ani.app.torrent.anitorrent.session

import kotlinx.io.files.Path
import me.him188.ani.app.torrent.anitorrent.binding.session_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_add_info_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_handle_t
import me.him188.ani.utils.io.toNioPath
import kotlin.io.path.absolutePathString

internal class SwigTorrentSession(
    internal val native: session_t,
) : TorrentSession<SwigTorrentHandle, SwigTorrentAddInfo> {
    override fun startDownload(handle: SwigTorrentHandle, addInfo: SwigTorrentAddInfo, saveDir: Path): Boolean =
        native.start_download(handle.native, addInfo.native, saveDir.toNioPath().absolutePathString())

    override fun releaseHandle(handle: SwigTorrentHandle) {
        native.release_handle(handle.native)
    }

    override fun createTorrentHandle(): SwigTorrentHandle = SwigTorrentHandle(torrent_handle_t())
    override fun createTorrentAddInfo(): SwigTorrentAddInfo = SwigTorrentAddInfo(torrent_add_info_t())
    override fun resume() {
        native.resume()
    }
}

internal class SwigTorrentHandle(
    val native: torrent_handle_t,
) : TorrentHandle {
    override fun addTracker(tracker: String, tier: Short, failLimit: Short) {
        native.add_tracker(tracker, tier, failLimit)
    }
}

internal class SwigTorrentAddInfo(
    val native: torrent_add_info_t,
) : TorrentAddInfo {
    override fun setMagnetUri(uri: String) {
        native.magnet_uri = uri
        native.kind = torrent_add_info_t.kKindMagnetUri
    }

    override fun setTorrentFilePath(absolutePath: String) {
        native.torrent_file_path = absolutePath
        native.kind = torrent_add_info_t.kKindTorrentFile
    }

    override fun setResumeDataPath(absolutePath: String) {
        native.resume_data_path = absolutePath
    }
}
