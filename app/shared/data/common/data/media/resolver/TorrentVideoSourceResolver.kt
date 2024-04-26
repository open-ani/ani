package me.him188.ani.app.data.media.resolver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.torrent.TorrentFileEntry
import me.him188.ani.app.torrent.api.FilePriority
import me.him188.ani.app.torrent.model.EncodedTorrentData
import me.him188.ani.app.videoplayer.data.OpenFailures
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.data.VideoSourceOpenException
import me.him188.ani.app.videoplayer.torrent.TorrentVideoData
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.contains
import me.him188.ani.datasources.api.topic.titles.RawTitleParser
import me.him188.ani.datasources.api.topic.titles.parse
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TorrentVideoSourceResolver(
    private val torrentManager: TorrentManager,
) : VideoSourceResolver {
    override fun supports(media: Media): Boolean {
        return media.download is ResourceLocation.HttpTorrentFile || media.download is ResourceLocation.MagnetLink
    }

    override suspend fun resolve(media: Media, episode: EpisodeMetadata): VideoSource<*> {
        return when (val location = media.download) {
            is ResourceLocation.HttpTorrentFile,
            is ResourceLocation.MagnetLink
            -> {
                TorrentVideoSource(
                    torrentManager.downloader.await().fetchTorrent(location.uri),
                    episode,
                )
            }

            else -> throw UnsupportedMediaException(media)
        }
    }

    companion object {
        private val DEFAULT_VIDEO_EXTENSIONS =
            setOf("mp4", "mkv", "avi", "mpeg", "mov", "flv", "wmv", "webm", "rm", "rmvb")

        fun selectVideoFileEntry(
            entries: List<TorrentFileEntry>,
            episodeTitles: List<String>,
            episodeSort: EpisodeSort,
            videoExtensions: Set<String> = DEFAULT_VIDEO_EXTENSIONS,
        ): TorrentFileEntry? {
            // Filter by file extension
            val videos = entries.filter {
                videoExtensions.any { fileType -> it.filePath.endsWith(fileType, ignoreCase = true) }
            }

            // Find by name match
            for (episodeTitle in episodeTitles) {
                val entry = videos.singleOrNull {
                    it.filePath.contains(episodeTitle, ignoreCase = true)
                }
                if (entry != null) return entry
            }

            // 解析标题匹配集数
            val parsedTitles = videos.associateWith {
                RawTitleParser.getDefault().parse(it.filePath.substringBeforeLast("."), null).episodeRange
            }
            if (parsedTitles.isNotEmpty()) {
                parsedTitles.entries.firstOrNull {
                    it.value?.contains(episodeSort) == true
                }?.key?.let { return it }
            }

            // 解析失败, 尽可能匹配一个
            episodeSort.toString().let { number ->
                videos.firstOrNull { it.filePath.contains(number, ignoreCase = true) }
                    ?.let { return it }
            }

            return videos.firstOrNull()
        }
    }
}

private class TorrentVideoSource(
    private val encodedTorrentData: EncodedTorrentData,
    private val episodeMetadata: EpisodeMetadata,
) : VideoSource<TorrentVideoData>, KoinComponent {
    private val manager: TorrentManager by inject()

    @OptIn(ExperimentalStdlibApi::class)
    override val uri: String by lazy {
        "torrent://${encodedTorrentData.data.toHexString()}"
    }

    override suspend fun open(): TorrentVideoData {
        return TorrentVideoData(
            withContext(Dispatchers.IO) {
                val files = manager.downloader.await().startDownload(encodedTorrentData)
                    .getFiles()

                logger.info {
                    "TorrentVideoSource opening a VideoData, metadata: $episodeMetadata, total ${files.size} files."
                }

                TorrentVideoSourceResolver.selectVideoFileEntry(
                    files,
                    listOf(episodeMetadata.title),
                    episodeMetadata.sort,
                )?.also {
                    logger.info {
                        "TorrentVideoSource selected file: ${it.filePath}"
                    }
                }?.createHandle()?.also {
                    it.resume(FilePriority.HIGH)
                } ?: throw VideoSourceOpenException(OpenFailures.NO_MATCHING_FILE)
            },
        )
    }

    override fun toString(): String = "TorrentVideoSource(uri=$uri, episodeMetadata=${episodeMetadata})"

    companion object {
        val logger = logger<TorrentVideoSource>()
    }
}
