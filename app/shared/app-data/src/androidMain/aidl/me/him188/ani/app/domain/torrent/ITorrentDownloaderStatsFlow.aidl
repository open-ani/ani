// ITorrentDownloaderStatsFlow.aidl
package me.him188.ani.app.domain.torrent;

import me.him188.ani.app.domain.torrent.parcel.PTorrentDownloaderStats;

// Declare any non-default types here with import statements

interface ITorrentDownloaderStatsFlow {
    void onEmit(out PTorrentDownloaderStats stat);
}