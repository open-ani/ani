package me.him188.ani.app.data.media.resolver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.him188.ani.app.tools.torrent.TorrentEngine
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import me.him188.ani.app.torrent.api.files.FilePriority
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

class TorrentVideoSourceResolver(
    private val engine: TorrentEngine,
) : VideoSourceResolver {
    override suspend fun supports(media: Media): Boolean {
        if (!engine.isEnabled.first()) return false
        return media.download is ResourceLocation.HttpTorrentFile || media.download is ResourceLocation.MagnetLink
    }

    override suspend fun resolve(media: Media, episode: EpisodeMetadata): VideoSource<*> {
        val downloader = engine.getDownloader() ?: throw UnsupportedMediaException(media)
        return when (val location = media.download) {
            is ResourceLocation.HttpTorrentFile,
            is ResourceLocation.MagnetLink
            -> {
                TorrentVideoSource(
                    engine,
                    encodedTorrentInfo = downloader.fetchTorrent(location.uri),
                    episodeMetadata = episode,
                )
            }

            else -> throw UnsupportedMediaException(media)
        }
    }

    companion object {
        private val DEFAULT_VIDEO_EXTENSIONS =
            setOf("mp4", "mkv", "avi", "mpeg", "mov", "flv", "wmv", "webm", "rm", "rmvb")

        /**
         * @param episodeSort 在系列中的集数, 例如第二季的第一集为 26
         * @param episodeEp 在当前季度中的集数, 例如第二季的第一集为 01
         */
        fun <T> selectVideoFileEntry(
            entries: List<T>,
            getPath: T.() -> String,
            episodeTitles: List<String>,
            episodeSort: EpisodeSort,
            episodeEp: EpisodeSort?,
            videoExtensions: Set<String> = DEFAULT_VIDEO_EXTENSIONS,
        ): T? {
            // Filter by file extension
            val videos = entries.filter {
                videoExtensions.any { fileType -> it.getPath().endsWith(fileType, ignoreCase = true) }
            }

            // Find by name match
            for (episodeTitle in episodeTitles) {
                val entry = videos.singleOrNull {
                    it.getPath().contains(episodeTitle, ignoreCase = true)
                }
                if (entry != null) return entry
            }

            // 解析标题匹配集数
            val parsedTitles = videos.associateWith {
                RawTitleParser.getDefault().parse(it.getPath().substringBeforeLast("."), null).episodeRange
            }
            // 优先按系列集数 sort 匹配 (数字较大)
            if (parsedTitles.isNotEmpty()) {
                parsedTitles.entries.firstOrNull {
                    it.value?.contains(episodeSort) == true
                }?.key?.let { return it }
            }
            // 然后按季度集数 ep 匹配
            if (episodeEp != null && parsedTitles.isNotEmpty()) {
                parsedTitles.entries.firstOrNull {
                    it.value?.contains(episodeEp) == true
                }?.key?.let { return it }
            }

            // 解析失败, 尽可能匹配一个
            episodeSort.toString().let { number ->
                videos.firstOrNull { it.getPath().contains(number, ignoreCase = true) }
                    ?.let { return it }
            }

            return videos.firstOrNull()
        }
    }
}

private class TorrentVideoSource(
    private val engine: TorrentEngine,
    private val encodedTorrentInfo: EncodedTorrentInfo,
    private val episodeMetadata: EpisodeMetadata,
) : VideoSource<TorrentVideoData>, KoinComponent {
    @OptIn(ExperimentalStdlibApi::class)
    override val uri: String by lazy {
        "torrent://${encodedTorrentInfo.data.toHexString()}"
    }

    @Throws(VideoSourceOpenException::class)
    override suspend fun open(): TorrentVideoData {
        val downloader = engine.getDownloader() ?: throw VideoSourceOpenException(OpenFailures.ENGINE_DISABLED)
        return TorrentVideoData(
            withContext(Dispatchers.IO) {
                val files = downloader.startDownload(encodedTorrentInfo)
                    .getFiles()

                logger.info {
                    "TorrentVideoSource opening a VideoData, metadata: $episodeMetadata, total ${files.size} files."
                }

                TorrentVideoSourceResolver.selectVideoFileEntry(
                    files,
                    { pathInTorrent },
                    listOf(episodeMetadata.title),
                    episodeSort = episodeMetadata.sort,
                    episodeEp = episodeMetadata.ep,
                )?.also {
                    logger.info {
                        "TorrentVideoSource selected file: ${it.pathInTorrent}"
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
