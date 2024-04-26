package me.him188.ani.app.torrent.torrent4j

import me.him188.ani.app.torrent.api.AbstractLockedTorrentDownloader
import me.him188.ani.app.torrent.api.EncodedTorrentInfo
import me.him188.ani.app.torrent.api.Torrent4jTorrentInfo
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentDownloaderConfig
import me.him188.ani.app.torrent.api.TorrentFileDownloader
import me.him188.ani.app.torrent.api.TorrentLibInfo
import me.him188.ani.app.torrent.api.handshakeClientVersionString
import me.him188.ani.app.torrent.api.peerFingerprintString
import me.him188.ani.app.torrent.api.userAgentString
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.libtorrent4j.AlertListener
import org.libtorrent4j.LibTorrent
import org.libtorrent4j.Priority
import org.libtorrent4j.SessionManager
import org.libtorrent4j.Sha1Hash
import org.libtorrent4j.TorrentFlags
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import java.io.File

internal class Libtorrent4jTorrentDownloader(
    cacheDirectory: File,
    sessionManager: LockedSessionManager,
    downloadFile: TorrentFileDownloader,
    isDebug: Boolean,
) : AbstractLockedTorrentDownloader<Torrent4jTorrentInfo>(cacheDirectory, sessionManager, downloadFile, isDebug) {
    override val vendor: TorrentLibInfo = TorrentLibInfo(
        vendor = "libtorrent",
        version = LibTorrent.version(),
    )

    override fun SessionManager.startDownload(ti: Torrent4jTorrentInfo, saveDirectory: File) {
        val priorities = Priority.array(Priority.IGNORE, ti.fileCount)
        download(
            ti.info,
            saveDirectory,
            null,
            priorities,
            null,
            TorrentFlags.AUTO_MANAGED,
            //                TorrentFlags.SEQUENTIAL_DOWNLOAD,//.or_(TorrentFlags.NEED_SAVE_RESUME)
        )
    }

    override fun decodeTorrentInfo(data: EncodedTorrentInfo): Torrent4jTorrentInfo =
        Torrent4jTorrentInfo.decodeFrom(data)

    override suspend fun closeSession(info: Torrent4jTorrentInfo) {
        val hash = info.infoHashHex
        dataToSession.remove(hash)?.close()
        sessionManager.use {
            val torrentName = info.name
            find(Sha1Hash.parseHex(hash))?.let { handle ->
                logger.debug { "[$torrentName] Close: remove from libtorrent SessionManager" }
                remove(handle)
                logger.debug { "[$torrentName] Close: removed" }
            } ?: run {
                logger.debug { "[$torrentName] Close: handle not found, ignoring" }
            }
        }
    }
}

private val logger = logger(TorrentDownloader::class)

/**
 * Creates a new [Libtorrent4jTorrentDownloader] instance.
 *
 * The returned instance must be closed when it is no longer needed.
 *
 * @see Libtorrent4jTorrentDownloader
 */
@Suppress("FunctionName")
fun Libtorrent4jTorrentDownloader(
    cacheDirectory: File,
    downloadFile: TorrentFileDownloader,
    config: TorrentDownloaderConfig = TorrentDownloaderConfig.Default,
): TorrentDownloader {
    val sessionManager = SessionManager()

    val listener = object : AlertListener {
        override fun types(): IntArray {
            return intArrayOf(AlertType.SESSION_STATS.swig(), AlertType.DHT_STATS.swig())
        }

        override fun alert(alert: Alert<*>) {
            if (alert.type() == AlertType.SESSION_STATS) {
                sessionManager.postDhtStats()
            }

//            if (alert.type() == AlertType.DHT_STATS) {
//                val nodes: Long = sessionManager.stats().dhtNodes()
//                // wait for at least 10 nodes in the DHT.
//                if (nodes >= 10) {
//                    dht.complete(Unit)
//                }
//            }
        }
    }
    sessionManager.addListener(listener)
    logger.info { "Starting SessionManager" }
    sessionManager.start()
    sessionManager.applySettings(sessionManager.settings().apply {
        isEnableDht = true
        isEnableLsd = true
        activeDhtLimit(300)
        activeTrackerLimit(50)
        activeSeeds(8)
        activeDownloads(8)
        connectionsLimit(500) // default was 200
        seedingOutgoingConnections(true) // default was true, just to make sure
        uploadRateLimit(0)
        downloadRateLimit(0)
        maxPeerlistSize(1000)
        dhtBootstrapNodes = setOf(
            dhtBootstrapNodes.split(",") + listOf(
                "router.utorrent.com:6881",
                "router.bittorrent.com:6881",
                "dht.transmissionbt.com:6881",
                "router.bitcomet.com:6881",
            )
        ).joinToString(",")

        logger.info { "peerFingerprint was: $peerFingerprintString" }
        logger.info { "user_agent was: $userAgentString" }
        logger.info { "handshake_client_version was: $handshakeClientVersionString" }

        peerFingerprintString = config.peerFingerprint
        userAgentString = config.userAgent
        config.clientHandshakeVersion?.let {
            handshakeClientVersionString = it
        }
        logger.info { "peerFingerprint set: $peerFingerprintString" }
        logger.info { "user_agent set: $userAgentString" }
        logger.info { "handshake_client_version set: $handshakeClientVersionString" }
    })
    logger.info { "postDhtStats" }
    sessionManager.postDhtStats()
    // No need to wait for DHT, some devices may not have access to the DHT network.

//    logger.info { "Waiting for nodes in DHT" }
//    val dhtResult = withTimeoutOrNull(30.seconds) {
//        dht.await()
//    }
//    if (dhtResult == null) {
//        logger.info { "DHT bootstrap failed" }
//        error("DHT bootstrap failed")
//    }
    sessionManager.removeListener(listener)

    return Libtorrent4jTorrentDownloader(
        cacheDirectory,
        LockedSessionManager(sessionManager),
        downloadFile,
        isDebug = config.isDebug,
    )
}