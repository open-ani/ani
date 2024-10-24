// IRemoteAniTorrentEngine.aidl
package me.him188.ani.app.domain.torrent;

import me.him188.ani.app.domain.torrent.IRemoteTorrentDownloader;
import me.him188.ani.app.domain.torrent.IAnitorrentConfigFlow;
import me.him188.ani.app.domain.torrent.IProxySettingsFlow;
import me.him188.ani.app.domain.torrent.ITorrentPeerConfigFlow;

// Declare any non-default types here with import statements

interface IRemoteAniTorrentEngine {
    IAnitorrentConfigFlow getAnitorrentConfigFlow();
    IProxySettingsFlow getProxySettingsFlow();
    ITorrentPeerConfigFlow getTorrentPeerConfigFlow();
    void setSaveDir(String saveDir);
    
    IRemoteTorrentDownloader getDownlaoder();
}