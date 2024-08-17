package me.him188.ani.app.tools.torrent.engines

import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.models.preference.ProxySettings
import me.him188.ani.app.data.source.media.fetch.toClientProxyConfig
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.app.platform.versionCode
import me.him188.ani.app.tools.torrent.AbstractTorrentEngine
import me.him188.ani.app.tools.torrent.TorrentEngineConfig
import me.him188.ani.app.tools.torrent.TorrentEngineType
import me.him188.ani.app.torrent.anitorrent.AnitorrentDownloaderFactory
import me.him188.ani.app.torrent.api.HttpFileDownloader
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentDownloaderConfig
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.ktor.createDefaultHttpClient
import me.him188.ani.utils.ktor.proxy
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info

@Serializable
class AnitorrentConfig(
    override val enabled: Boolean
) : TorrentEngineConfig {
    companion object {
        val Default = AnitorrentConfig(enabled = true)
    }
}

// 只有启用构建 (gradle property `ani.enable.anitorrent=true`) 后才会启用 anitorrent 所在目录

//private val anitorrentFactory = java.util.ServiceLoader.load(TorrentDownloaderFactory::class.java)
//    .firstOrNull { it.name == "Anitorrent" }
private val anitorrentFactory = AnitorrentDownloaderFactory()

class AnitorrentEngine(
    scope: CoroutineScope,
    config: Flow<AnitorrentConfig>,
    private val proxySettings: Flow<ProxySettings>,
    private val saveDir: SystemPath,
) : AbstractTorrentEngine<TorrentDownloader, AnitorrentConfig>(
    scope = scope,
    type = TorrentEngineType.Anitorrent,
    config = config,
) {
    override val location: MediaSourceLocation get() = MediaSourceLocation.Local
    override val isSupported: Flow<Boolean>
        get() = flowOf(tryLoadLibraries())
    override val isEnabled: Flow<Boolean> get() = config.map { it.enabled }

    private fun tryLoadLibraries(): Boolean {
        try {
            anitorrentFactory.libraryLoader.loadLibraries()
            logger.info { "Loaded libraries for AnitorrentEngine" }
            return true
        } catch (e: Throwable) {
            logger.error(e) { "Failed to load libraries for AnitorrentEngine" }
            return false
        }
    }

    override suspend fun testConnection(): Boolean = true

    override suspend fun newInstance(config: AnitorrentConfig): TorrentDownloader {
        if (!isSupported.first()) {
            logger.error { "Anitorrent is disabled because it is not built. Read `/torrent/anitorrent/README.md` for more information." }
            throw UnsupportedOperationException("AnitorrentEngine is not supported")
        }
        val proxy = proxySettings.first()
        val client = createDefaultHttpClient {
            install(UserAgent) {
                agent = getAniUserAgent()
            }
            proxy(proxy.default.toClientProxyConfig())
            expectSuccess = true
        }
        return anitorrentFactory.createDownloader(
            rootDataDirectory = saveDir,
            client.asHttpFileDownloader(),
            TorrentDownloaderConfig(
                peerFingerprint = computeTorrentFingerprint(),
                userAgent = computeTorrentUserAgent(),
                isDebug = currentAniBuildConfig.isDebug,
            ),
            parentCoroutineContext = scope.coroutineContext,
        )
    }
}

private fun computeTorrentFingerprint(
    versionCode: String = currentAniBuildConfig.versionCode,
): String = "-aniLT${versionCode}-"

private fun computeTorrentUserAgent(
    versionCode: String = currentAniBuildConfig.versionCode,
): String = "ani_libtorrent/${versionCode}"

private fun HttpClient.asHttpFileDownloader(): HttpFileDownloader = object : HttpFileDownloader {
    override suspend fun download(url: String): ByteArray = get(url).readBytes()
    override fun close() {
        this@asHttpFileDownloader.close()
    }

    override fun toString(): String {
        return "HttpClientAsHttpFileDownloader(client=$this@asHttpFileDownloader)"
    }
}
