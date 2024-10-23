// ITorrentFileEntryStatsFlow.aidl
package me.him188.ani.app.domain.torrent;

import me.him188.ani.app.domain.torrent.parcel.PTorrentFileEntryStats;

// Declare any non-default types here with import statements

interface ITorrentFileEntryStatsFlow {
    void onEmit(out PTorrentFileEntryStats stat);
}