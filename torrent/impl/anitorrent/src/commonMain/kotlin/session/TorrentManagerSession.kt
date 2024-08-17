package me.him188.ani.app.torrent.anitorrent.session

import kotlinx.io.files.Path
import me.him188.ani.app.torrent.anitorrent.HandleId
import me.him188.ani.app.torrent.api.files.FilePriority

/**
 * libtorrent 的 session_t, 用来管理多个 torrent 任务
 */
interface TorrentManagerSession<Handle : TorrentHandle, AddInfo : TorrentAddInfo> {
    fun createTorrentHandle(): Handle
    fun createTorrentAddInfo(): AddInfo

    fun startDownload(handle: Handle, addInfo: AddInfo, saveDir: Path): Boolean
    fun releaseHandle(handle: Handle)

    fun resume()
}

/**
 * Native handle
 */
interface TorrentHandle {
    val id: HandleId

    val isValid: Boolean

    fun postStatusUpdates()
    fun postSaveResume()

    fun resume()
    fun setFilePriority(index: Int, priority: FilePriority)

    fun reloadFile(): TorrentDescriptor

    fun setPieceDeadline(index: Int, deadline: Int)
    fun clearPieceDeadlines()

    fun addTracker(tracker: String, tier: Short = 0, failLimit: Short = 0)
}

interface TorrentAddInfo {
    fun setMagnetUri(uri: String)
    fun setTorrentFilePath(absolutePath: String)

    fun setResumeDataPath(absolutePath: String)
}

