package me.him188.ani.app.torrent.anitorrent

import me.him188.ani.app.torrent.api.TorrentLibraryLoader

internal actual fun getAnitorrentTorrentLibraryLoader(): TorrentLibraryLoader {
    return AnitorrentLibraryLoader
}
