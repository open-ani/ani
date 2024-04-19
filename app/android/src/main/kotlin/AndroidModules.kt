package me.him188.ani.android

import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import me.him188.ani.android.navigation.AndroidBrowserNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.app.tools.torrent.DefaultTorrentManager
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.tools.torrent.computeTorrentFingerprint
import me.him188.ani.app.tools.torrent.computeTorrentUserAgent
import me.him188.ani.app.torrent.TorrentDownloader
import me.him188.ani.app.torrent.TorrentDownloaderConfig
import me.him188.ani.app.videoplayer.ExoPlayerStateFactory
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import org.koin.dsl.module
import java.io.File

fun getAndroidModules(
    torrentCacheDir: File,
    coroutineScope: CoroutineScope,
) = module {
    single<BrowserNavigator> { AndroidBrowserNavigator() }
    single<TorrentManager> {
        val client = HttpClient {
            followRedirects = true
            install(UserAgent) {
                agent = getAniUserAgent()
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        DefaultTorrentManager(
            coroutineScope.coroutineContext,
            downloaderFactory = {
                TorrentDownloader(
                    cacheDirectory = torrentCacheDir,
                    downloadFile = { url ->
                        client.get(url).apply {
                            check(status.isSuccess()) {
                                "Failed to download torrent file, resp=${
                                    bodyAsChannel().readRemaining().readBytes()
                                }"
                            }
                        }.bodyAsChannel().readRemaining().readBytes()
                    },
                    config = TorrentDownloaderConfig(
                        peerFingerprint = computeTorrentFingerprint(),
                        userAgent = computeTorrentUserAgent(),
                        isDebug = currentAniBuildConfig.isDebug,
                    )
                )
            }
        )
    }
    single<PlayerStateFactory> { ExoPlayerStateFactory() }
}