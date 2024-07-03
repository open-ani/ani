package me.him188.ani.app.torrent.anitorrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.app.torrent.anitorrent.binding.SWIGTYPE_p_libtorrent__session
import me.him188.ani.app.torrent.anitorrent.binding.anitorrent
import me.him188.ani.app.torrent.anitorrent.binding.torrent_handle_t
import me.him188.ani.app.torrent.api.FetchTorrentTimeoutException
import me.him188.ani.app.torrent.api.HttpFileDownloader
import me.him188.ani.app.torrent.api.TorrentDownloadSession
import me.him188.ani.app.torrent.api.TorrentDownloadState
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentLibInfo
import me.him188.ani.app.torrent.api.files.AbstractTorrentFileEntry
import me.him188.ani.app.torrent.api.files.DownloadStats
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import me.him188.ani.app.torrent.api.files.TorrentFileEntry
import me.him188.ani.app.torrent.api.files.TorrentFileHandle
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.libtorrent4j.DefaultTorrentDownloadSession
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

fun AnitorrentTorrentDownloader(
    cacheDirectory: File,
    httpFileDownloader: HttpFileDownloader,
    isDebug: Boolean,
    parentCoroutineContext: CoroutineContext,
): AnitorrentTorrentDownloader {
    if (currentPlatform is Platform.Windows) {
    } else {
        // TODO: this doesn't work 
        System.setProperty(
            "java.library.path",
            System.getProperty("java.library.path") + ":" +
                    File(System.getProperty("user.dir")).resolve("../appResources/macos-arm64/lib").absolutePath,
        )
    }
    System.loadLibrary("anitorrent")

    println("Using libtorrent version: " + anitorrent.lt_version())

    val session = anitorrent.new_session(getAniUserAgent())
    return AnitorrentTorrentDownloader(
        cacheDirectory = cacheDirectory,
        session = session,
        httpFileDownloader = httpFileDownloader,
        isDebug = isDebug,
        parentCoroutineContext = parentCoroutineContext,
    )
}

class AnitorrentTorrentDownloader(
    cacheDirectory: File,
    val session: SWIGTYPE_p_libtorrent__session,
    private val httpFileDownloader: HttpFileDownloader,
    private val isDebug: Boolean,
    parentCoroutineContext: CoroutineContext,
) : TorrentDownloader {
    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))

    private val logger = logger(this::class)

    override val totalUploaded: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val totalDownloaded: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val totalUploadRate: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val totalDownloadRate: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val vendor: TorrentLibInfo = TorrentLibInfo(
        vendor = "Anitorrent",
        version = "1.0.0",
        supportsStreaming = true,
    )

    init {
        scope.launch {
            while (currentCoroutineContext().isActive) {
                // TODO: stats 
//                val stats = sessionManager.getStats()
//                totalUploaded.value = stats.totalUpload()
//                totalDownloaded.value = stats.totalDownload()
//                totalUploadRate.value = stats.uploadRate()
//                totalDownloadRate.value = stats.downloadRate()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun fetchTorrent(uri: String, timeoutSeconds: Int): EncodedTorrentInfo {
        if (uri.startsWith("http", ignoreCase = true)) {
            val cacheFile = getHttpTorrentFileCacheFile(uri)
            if (cacheFile.exists()) {
                val data = cacheFile.readText().hexToByteArray()
                logger.info { "HTTP torrent file '${uri}' found in cache: $cacheFile, length=${data.size}" }
                return AnitorrentTorrentInfo.encode(uri, data)
            }
            logger.info { "Fetching http url: $uri" }
            val data = httpFileDownloader.download(uri)
            logger.info { "Fetching http url success, file length = ${data.size}" }
            cacheFile.writeText(data.toHexString())
            logger.info { "Saved cache file: $cacheFile" }
            return AnitorrentTorrentInfo.encode(uri, data)
        }

        logger.info { "Fetching magnet: $uri" }
        val str: String = try {
            anitorrent.fetch_magnet(session, uri, timeoutSeconds, magnetCacheDir.absolutePath)
        } catch (e: InterruptedException) {
            throw FetchTorrentTimeoutException(cause = e)
        }
        val data = str.toByteArray()
        if (data.isEmpty()) {
            throw FetchTorrentTimeoutException()
        }
        logger.info { "Fetched magnet: size=${data.size}" }
        return AnitorrentTorrentInfo.encode(uri, data)
    }

    private val magnetCacheDir = cacheDirectory.resolve("magnet").apply {
        mkdirs()
    }

    private val httpTorrentFileCacheDir = cacheDirectory.resolve("torrentFiles").apply {
        mkdirs()
    }

    private fun getHttpTorrentFileCacheFile(uri: String): File {
        return httpTorrentFileCacheDir.resolve(uri.hashCode().toString() + ".txt")
    }

    private val downloadCacheDir = cacheDirectory.resolve("api/pieces").apply {
        mkdirs()
    }

    protected val dataToSession = ConcurrentHashMap<String, DefaultTorrentDownloadSession>()

    private val lock = Mutex()

    override suspend fun startDownload(
        data: EncodedTorrentInfo,
        parentCoroutineContext: CoroutineContext,
        overrideSaveDir: File?
    ): TorrentDownloadSession {
        val info = AnitorrentTorrentInfo.decodeFrom(data)
            ?: throw IllegalArgumentException("Invalid torrent data, native failed to decode")
        val saveDir = overrideSaveDir ?: getSaveDirForTorrent(data)

        val handle = torrent_handle_t()
        if (!anitorrent.start_download(session, handle, info.native, saveDir.absolutePath)) {
            throw IllegalStateException("Failed to start download, native failed")
        }
        return AnitorrentDownloadSession(this.session, handle, saveDir)
    }

    override fun getSaveDirForTorrent(data: EncodedTorrentInfo): File =
        downloadCacheDir.resolve(data.data.contentHashCode().toString())

    override fun listSaves(): List<File> {
        return downloadCacheDir.listFiles()?.toList() ?: emptyList()
    }

    override fun close() {
        scope.cancel()
        httpFileDownloader.close()
    }
}

class AnitorrentDownloadSession(
    private val session: SWIGTYPE_p_libtorrent__session,
    private val handle: torrent_handle_t,
    override val saveDirectory: File,
) : TorrentDownloadSession {
    override val state: MutableStateFlow<TorrentDownloadState> = MutableStateFlow(TorrentDownloadState.Starting)
    override val overallStats: DownloadStats = object : DownloadStats() {
        override val totalBytes: MutableStateFlow<Long> = MutableStateFlow(0)
        override val uploadRate: MutableStateFlow<Long?> = MutableStateFlow(0)
        override val progress: MutableStateFlow<Float> = MutableStateFlow(0f)
    }

    inner class AnitorrentFileEntry(
        override val pieces: List<Piece>,
        override val supportsStreaming: Boolean,
        index: Int, length: Long, saveDirectory: File, relativePath: String,
        torrentName: String, isDebug: Boolean, parentCoroutineContext: CoroutineContext
    ) : AbstractTorrentFileEntry(
        index, length, saveDirectory, relativePath, torrentName, isDebug,
        parentCoroutineContext,
    ) {
        override fun updatePriority() {
            TODO("Not yet implemented")
        }

        override val stats: DownloadStats = object : DownloadStats() {
            override val totalBytes: MutableStateFlow<Long> = MutableStateFlow(0)
            override val uploadRate: MutableStateFlow<Long?> = MutableStateFlow(0)
            override val progress: MutableStateFlow<Float> = MutableStateFlow(0f)
        }

        override fun createHandle(): TorrentFileHandle {
            TODO("Not yet implemented")
        }

        override fun createInput(): SeekableInput {
            TODO("Not yet implemented")
        }
    }


    override suspend fun getFiles(): List<TorrentFileEntry> {
//        return anitorrent.get_files(handle).map {
//            TorrentFileEntry(
//                name = it.name,
//                size = it.size,
//                priority = it.priority,
//                progress = it.progress,
//            )
//        }
        return emptyList()
    }

    override fun close() {
    }

    override fun closeIfNotInUse() {
    }
}
