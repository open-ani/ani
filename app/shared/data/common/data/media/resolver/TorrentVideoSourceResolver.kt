package me.him188.ani.app.data.media.resolver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.torrent.model.EncodedTorrentData
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.torrent.TorrentVideoData
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.ResourceLocation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TorrentVideoSourceResolver(
    private val torrentManager: TorrentManager,
) : VideoSourceResolver {
    override fun supports(media: Media): Boolean {
        return media.download is ResourceLocation.HttpTorrentFile || media.download is ResourceLocation.MagnetLink
    }

    override suspend fun resolve(media: Media, episode: EpisodeSort): VideoSource<*> {
        return when (val location = media.download) {
            is ResourceLocation.HttpTorrentFile,
            is ResourceLocation.MagnetLink
            -> {
                TorrentVideoSource(
                    torrentManager.downloader.await().fetchTorrent(location.uri)
                )
            }

            else -> throw UnsupportedMediaException(media)
        }
    }
}

private class TorrentVideoSource(
    private val encodedTorrentData: EncodedTorrentData,
) : VideoSource<TorrentVideoData>, KoinComponent {
    private val manager: TorrentManager by inject()

    @OptIn(ExperimentalStdlibApi::class)
    override val uri: String by lazy {
        "torrent://${encodedTorrentData.data.toHexString()}"
    }

    override suspend fun open(): TorrentVideoData {
        return TorrentVideoData(withContext(Dispatchers.IO) {
            manager.downloader.await().startDownload(encodedTorrentData)
        })
    }

    override fun toString(): String = "TorrentVideoSource(uri=$uri)"
}
