package me.him188.ani.app.tools.torrent.engines

import io.ktor.client.plugins.UserAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import me.him188.ani.app.platform.Platform.Companion.currentPlatform
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.app.tools.torrent.AbstractTorrentEngine
import me.him188.ani.app.tools.torrent.TorrentEngineConfig
import me.him188.ani.app.tools.torrent.TorrentEngineType
import me.him188.ani.app.torrent.anitorrent.AnitorrentTorrentDownloader
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.utils.ktor.createDefaultHttpClient
import java.io.File

@Serializable
class AnitorrentConfig(
    override val enabled: Boolean
) : TorrentEngineConfig {
    companion object {
        val Default = AnitorrentConfig(enabled = true)
    }
}

class AnitorrentEngine(
    scope: CoroutineScope,
    config: Flow<AnitorrentConfig>,
    private val saveDir: File,
) : AbstractTorrentEngine<AnitorrentTorrentDownloader, AnitorrentConfig>(
    scope = scope,
    type = TorrentEngineType.QBittorrent,
    config = config,
) {

    override val location: MediaSourceLocation get() = MediaSourceLocation.Local
    override val isSupported: Flow<Boolean> get() = flowOf(currentPlatform.isDesktop())
    override val isEnabled: Flow<Boolean> get() = config.map { it.enabled }

    override suspend fun testConnection(): Boolean = true

    override suspend fun newInstance(config: AnitorrentConfig): AnitorrentTorrentDownloader {
        val client = createDefaultHttpClient {
            install(UserAgent) {
                agent = getAniUserAgent()
            }
            expectSuccess = true
        }
        return AnitorrentTorrentDownloader(
            cacheDirectory = saveDir,
            client.asHttpFileDownloader(),
            isDebug = false,
            parentCoroutineContext = scope.coroutineContext,
        )
    }
}
