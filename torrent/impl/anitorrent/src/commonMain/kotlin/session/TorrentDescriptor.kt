package me.him188.ani.app.torrent.anitorrent.session

interface TorrentDescriptor {
    val name: String

    val fileCount: Int
    fun fileAtOrNull(index: Int): TorrentFileInfo?

    val numPieces: Int
    val lastPieceSize: Long
    val pieceLength: Long
}

interface TorrentFileInfo {
    val name: String
    val path: String
    val size: Long
}