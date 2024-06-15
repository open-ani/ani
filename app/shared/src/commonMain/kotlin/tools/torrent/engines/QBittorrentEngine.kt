package me.him188.ani.app.tools.torrent.engines

import io.ktor.client.plugins.UserAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.app.platform.Platform.Companion.currentPlatform
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.app.tools.torrent.AbstractTorrentEngine
import me.him188.ani.app.tools.torrent.TorrentEngineConfig
import me.him188.ani.app.tools.torrent.TorrentEngineType
import me.him188.ani.app.torrent.qbittorrent.QBittorrentClient
import me.him188.ani.app.torrent.qbittorrent.QBittorrentClientConfig
import me.him188.ani.app.torrent.qbittorrent.QBittorrentTorrentDownloader
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.utils.ktor.createDefaultHttpClient
import java.io.File

@Serializable
data class QBittorrentConfig(
    override val enabled: Boolean = currentPlatform.isDesktop(),
    val clientConfig: QBittorrentClientConfig = QBittorrentClientConfig(
        userAgent = getAniUserAgent()
    ),
    @Transient val placeholder: Int = 0,
) : TorrentEngineConfig {
    companion object {
        val Default = QBittorrentConfig()
    }
}

class QBittorrentEngine(
    scope: CoroutineScope,
    config: Flow<QBittorrentConfig>,
    private val saveDir: File,
) : AbstractTorrentEngine<QBittorrentTorrentDownloader, QBittorrentConfig>(
    scope = scope,
    type = TorrentEngineType.QBittorrent,
    config = config
) {
    override val location: MediaSourceLocation get() = MediaSourceLocation.Local
    override val isSupported: Flow<Boolean> get() = flowOf(currentPlatform.isDesktop())
    override val isEnabled: Flow<Boolean> get() = config.map { it.enabled }

    override suspend fun testConnection(): Boolean {
        QBittorrentClient(config.first().clientConfig).use {
            try {
                it.getVersion()
                return true
            } catch (e: Exception) {
                return false
            }
        }
    }

    override suspend fun newInstance(config: QBittorrentConfig): QBittorrentTorrentDownloader {
        val client = createDefaultHttpClient {
            install(UserAgent) {
                agent = getAniUserAgent()
            }
            expectSuccess = true
        }
        return QBittorrentTorrentDownloader(
            config = config.clientConfig,
            saveDir = saveDir,
            client.asHttpFileDownloader(),
            parentCoroutineContext = scope.coroutineContext
        )
    }
}
