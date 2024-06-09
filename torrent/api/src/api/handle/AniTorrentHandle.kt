package me.him188.ani.app.torrent.api.handle

import me.him188.ani.app.torrent.api.files.FilePriority
import me.him188.ani.app.torrent.api.pieces.Piece

interface AniTorrentHandle {
    val name: String

    fun addTracker(url: String)

    val contents: TorrentContents

    fun resume()
    fun pause()

    fun setPieceDeadline(pieceIndex: Int, deadline: Int)
    fun saveResumeData() {}
}

interface TorrentContents {
    @TorrentThread
    fun createPieces(): List<Piece>

    @TorrentThread
    val files: List<TorrentFile>

    @TorrentThread
    fun getFileProgresses(): List<Pair<TorrentFile, Long>>
}

interface TorrentFile {
    val path: String
    val size: Long

    @TorrentThread
    var priority: FilePriority
}


/**
 * 标记一个 API, 必须在 BT 线程中调用.
 *
 * 访问 libtorrent 的 API 必须在 BT 线程中调用, 否则会导致 native crash.
 */
@Target(
    AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.CONSTRUCTOR
)
@RequiresOptIn(message = "This function must be accessed in torrent thread")
annotation class TorrentThread
