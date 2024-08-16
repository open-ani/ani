package me.him188.ani.app.torrent.anitorrent.session

import me.him188.ani.app.torrent.api.files.FilePriority

interface AnitorrentHandle {
    val id: Any

    val isValid: Boolean

    fun postStatusUpdates()
    fun postSaveResume()

    fun resume()
    fun setFilePriority(index: Int, priority: FilePriority)

    fun reloadFile(): TorrentDescriptor

    fun setPieceDeadline(index: Int, deadline: Int)
    fun clearPieceDeadlines()
}

