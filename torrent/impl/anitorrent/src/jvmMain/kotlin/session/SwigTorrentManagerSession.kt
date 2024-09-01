package me.him188.ani.app.torrent.anitorrent.session

import kotlinx.io.files.Path
import me.him188.ani.app.torrent.anitorrent.binding.session_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_add_info_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_handle_t
import me.him188.ani.app.torrent.anitorrent.toSessionSettings
import me.him188.ani.app.torrent.api.TorrentDownloaderConfig
import me.him188.ani.utils.io.toNioPath
import kotlin.io.path.absolutePathString

internal class SwigTorrentManagerSession(
    internal val native: session_t,
) : TorrentManagerSession<SwigTorrentHandle, SwigTorrentAddInfo> {
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

    override fun applyConfig(config: TorrentDownloaderConfig) {
        native.apply_settings(config.toSessionSettings())
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
