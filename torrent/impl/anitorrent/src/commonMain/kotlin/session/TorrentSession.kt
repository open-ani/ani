package me.him188.ani.app.torrent.anitorrent.session

import kotlinx.io.files.Path

interface TorrentSession<Handle : TorrentHandle, AddInfo : TorrentAddInfo> {
    fun createTorrentHandle(): Handle
    fun createTorrentAddInfo(): AddInfo

    fun startDownload(handle: Handle, addInfo: AddInfo, saveDir: Path): Boolean
    fun releaseHandle(handle: Handle)

    fun resume()
}

interface TorrentHandle {
    fun addTracker(tracker: String, tier: Short = 0, failLimit: Short = 0)
}

interface TorrentAddInfo {
    fun setMagnetUri(uri: String)
    fun setTorrentFilePath(absolutePath: String)

    fun setResumeDataPath(absolutePath: String)
}

