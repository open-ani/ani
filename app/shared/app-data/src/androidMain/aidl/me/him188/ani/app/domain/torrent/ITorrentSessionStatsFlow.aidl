// ITorrentSessionStatsFlow.aidl
package me.him188.ani.app.domain.torrent;

import me.him188.ani.app.domain.torrent.parcel.PTorrentSessionStats;

// Declare any non-default types here with import statements

interface ITorrentSessionStatsFlow {
    void onEmit(out PTorrentSessionStats stat);
}