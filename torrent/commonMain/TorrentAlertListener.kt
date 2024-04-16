package me.him188.ani.app.torrent

import org.libtorrent4j.alerts.TorrentAlert

public interface TorrentAlertListener {
    public fun onAlert(alert: TorrentAlert<*>)
}