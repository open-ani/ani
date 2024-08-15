package me.him188.ani.app.torrent.anitorrent.session

import kotlinx.io.files.Path

interface TorrentResumeData {
    fun saveToPath(path: Path)
} 
