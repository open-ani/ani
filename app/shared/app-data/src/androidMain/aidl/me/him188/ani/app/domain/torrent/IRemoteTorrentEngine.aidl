// IRemoteTorrentEngine.aidl
package me.him188.ani.app.domain.torrent;

import me.him188.ani.app.domain.torrent.IRemoteTorrentDownloader;

// Declare any non-default types here with import statements

interface IRemoteTorrentEngine {
    IRemoteTorrentDownloader getDownlaoder();
}