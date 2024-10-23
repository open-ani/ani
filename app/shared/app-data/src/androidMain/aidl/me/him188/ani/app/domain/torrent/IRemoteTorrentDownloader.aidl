// IRemoteTorrentDownloader.aidl
package me.him188.ani.app.domain.torrent;

import me.him188.ani.app.domain.torrent.ITorrentDownloaderStatsFlow;
import me.him188.ani.app.domain.torrent.IRemoteTorrentSession;
import me.him188.ani.app.domain.torrent.IDisposableHandle;
import me.him188.ani.app.domain.torrent.parcel.PTorrentLibInfo;
import me.him188.ani.app.domain.torrent.parcel.PEncodedTorrentInfo;

// Declare any non-default types here with import statements

interface IRemoteTorrentDownloader {
    IDisposableHandle getTotalStatus(ITorrentDownloaderStatsFlow flow);
    
    PTorrentLibInfo getVendor();
    
    PEncodedTorrentInfo fetchTorrent(in String uri, int timeoutSeconds);
    
    IRemoteTorrentSession startDownload(in PEncodedTorrentInfo data, in String overrideSaveDir);
    
    String getSaveDirForTorrent();
    
    String[] listSaves();
    
    void close();
}