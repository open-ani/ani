package me.him188.ani.app.torrent.qbittorrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.torrent.api.TorrentDownloadSession
import me.him188.ani.app.torrent.api.TorrentDownloadState
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentLibInfo
import me.him188.ani.app.torrent.api.files.AbstractTorrentFileEntry
import me.him188.ani.app.torrent.api.files.DownloadStats
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import me.him188.ani.app.torrent.api.files.FilePriority
import me.him188.ani.app.torrent.api.files.TorrentFileEntry
import me.him188.ani.app.torrent.api.files.TorrentFileHandle
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.io.TorrentFileIO
import me.him188.ani.utils.coroutines.SuspendLazy
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.toSeekableInput
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.math.absoluteValue

/**
 * 连接 qBittorrent API 的下载器.
 */
class QBittorrentTorrentDownloader(
    config: QBittorrentClientConfig,
    private val saveDir: File,
    parentCoroutineContext: CoroutineContext,
) : TorrentDownloader {
    companion object {
        private val logger = logger<QBittorrentTorrentDownloader>()
    }

    private val qBittorrentClientLazy = lazy { QBittorrentClient(config) }
    private val client by qBittorrentClientLazy

    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))

    init {
        scope.coroutineContext[Job]?.invokeOnCompletion {
            close()
        }
        scope.launch {
            while (isActive) {
                try {
                    client.getGlobalTransferInfo().let { info ->
                        totalUploaded.value = info.upInfoData
                        totalDownloaded.value = info.dlInfoData
                        totalUploadRate.value = info.upInfoSpeed
                        totalDownloadRate.value = info.dlInfoSpeed
                    }
                } catch (e: Throwable) {
                    logger.error(e) { "Failed to update qBittorrent stats" }
                }
                delay(1000)
            }
        }
    }

    override val totalUploaded: MutableStateFlow<Long> = MutableStateFlow(0)
    override val totalDownloaded: MutableStateFlow<Long> = MutableStateFlow(0)
    override val totalUploadRate: MutableStateFlow<Long> = MutableStateFlow(0)
    override val totalDownloadRate: MutableStateFlow<Long> = MutableStateFlow(0)
    override val vendor: TorrentLibInfo = TorrentLibInfo("qBittorrent", "4.3.6", supportsStreaming = false)

    override suspend fun fetchTorrent(uri: String, timeoutSeconds: Int): EncodedTorrentInfo {
        val tempDirection = saveDir.resolve(uri.hashCode().absoluteValue.toString()).apply {
            mkdirs()
        }
        try {
            client.addTorrentFromUri(uri, savePath = tempDirection.absolutePath, paused = true)
            val actual = awaitTorrentData { it.savePath == tempDirection.absolutePath }
            client.deleteTorrents(listOf(actual.hash), false)
            return EncodedTorrentInfo(actual.magnetUri.toByteArray())
        } finally {
            tempDirection.deleteRecursively()
        }
    }

    private suspend fun awaitTorrentData(predicate: (QBTorrent) -> Boolean): QBTorrent {
        while (true) {
            val list = client.getTorrentList()
            val torrent = list.firstOrNull(predicate)
            if (torrent != null) {
                return torrent
            }
            delay(1000)
        }
    }

    override suspend fun startDownload(
        data: EncodedTorrentInfo,
        parentCoroutineContext: CoroutineContext
    ): TorrentDownloadSession {
        // note: this hash is not equivalent to torrent info hash
        val dir = getSaveDirForTorrent(data).apply { mkdirs() }
        client.getTorrentList().firstOrNull { it.savePath == dir.absolutePath }?.let {
            return QBittorrentTorrentDownloadSession(
                torrentInfo = it,
                client = client,
                saveDirectory = dir,
                parentCoroutineContext = parentCoroutineContext
            )
        }
        val uri = data.data.decodeToString()
        check(uri.startsWith("magnet"))
        client.addTorrentFromUri(
            uri, // magnet
            savePath = dir.absolutePath,
            paused = false
        )
        return QBittorrentTorrentDownloadSession(
            torrentInfo = awaitTorrentData { it.savePath == dir.absolutePath },
            client = client,
            saveDirectory = dir,
            parentCoroutineContext = parentCoroutineContext
        )
    }

    override fun getSaveDirForTorrent(data: EncodedTorrentInfo): File =
        saveDir.resolve(TorrentFileIO.hashSha1(data.data))

    override fun listSaves(): List<File> = saveDir.listFiles()?.toList() ?: emptyList()

    @Volatile
    private var isClosed = false
    override fun close() {
        if (isClosed) return
        synchronized(this) {
            if (isClosed) return
            isClosed = true
        }

        if (qBittorrentClientLazy.isInitialized()) {
            qBittorrentClientLazy.value.close()
        }
    }
}

class QBittorrentTorrentDownloadSession(
    private var torrentInfo: QBTorrent,
    private val client: QBittorrentClient,
    override val saveDirectory: File,
    parentCoroutineContext: CoroutineContext
) : TorrentDownloadSession {
    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))
    private val logger = logger(this.toString())


    override val state = MutableStateFlow<TorrentDownloadState>(TorrentDownloadState.Starting)

    private val overallStatsImpl = object : DownloadStats() {
        override val totalBytes = MutableStateFlow(torrentInfo.totalSize)
        override val downloadedBytes = MutableStateFlow(torrentInfo.downloaded)
        override val downloadRate = MutableStateFlow<Long?>(null)
        override val uploadRate = MutableStateFlow<Long?>(null)
        override val progress = MutableStateFlow(0f)
        override val isFinished = progress.map { it == 1f }

        fun updateFrom(torrentInfo: QBTorrent) {
            totalBytes.value = torrentInfo.totalSize
            downloadedBytes.value = torrentInfo.downloaded
            downloadRate.value = torrentInfo.dlSpeed
            uploadRate.value = torrentInfo.upSpeed
            progress.value = torrentInfo.progress
        }
    }
    override val overallStats get() = overallStatsImpl

    init {
        scope.launch {
            while (isActive) {
                try {
                    client.getTorrentList(hashes = listOf(torrentInfo.hash)).firstOrNull()?.let {
                        torrentInfo = it
                        overallStatsImpl.updateFrom(it)
                    }
                } catch (e: Throwable) {
                    logger.error(e) { "Failed to update qBittorrent stats" }
                }
                delay(1000)
            }
        }
    }

    private val files = SuspendLazy {
        // 文件列表可能有延迟, 需要等
        val files = flow {
            emit(client.getTorrentFiles(torrentInfo.hash))
        }.onEach {
            if (it.isEmpty()) {
                throw IllegalStateException("No files found for torrent ${torrentInfo.name}")
            }
        }.retry {
            delay(1000)
            true
        }.first() // 一直重试直到获取到文件列表

        if (files.isEmpty()) {
            logger.warn { "No files found for torrent ${torrentInfo.name}" }
        }
        files.map { entry ->
            FileEntryImpl(
                index = entry.index,
                length = entry.size,
                saveDirectory = saveDirectory,
                relativePath = entry.name,
                torrentName = torrentInfo.name,
                isDebug = false,
                parentCoroutineContext = parentCoroutineContext
            )
        }
    }

    private val openHandles = ConcurrentLinkedQueue<FileEntryImpl.FileHandleImpl>()

    private inner class FileEntryImpl(
        index: Int,
        length: Long,
        saveDirectory: File,
        relativePath: String,
        torrentName: String,
        isDebug: Boolean,
        parentCoroutineContext: CoroutineContext,
    ) : AbstractTorrentFileEntry(
        index, length,
        saveDirectory,
        relativePath,
        torrentName, isDebug, parentCoroutineContext
    ) {
        inner class FileHandleImpl : AbstractTorrentFileHandle() {
            override fun closeImpl() {
                openHandles.remove(this)
            }

            override fun resumeImpl(priority: FilePriority) {
                scope.launch {
                    client.setFilePriority(torrentInfo.hash, index, priority.toQBFilePriority())
                }
            }
        }

        override val pieces: List<Piece>? = null
        private val priorityTasker = MonoTasker(scope)
        override fun updatePriority() {
            val highestPriority = priorityRequests.values.maxWithOrNull(nullsFirst(naturalOrder()))
                ?: FilePriority.IGNORE

            priorityTasker.launch {
                client.setFilePriority(torrentInfo.hash, index, highestPriority.toQBFilePriority())
                logger.info { "[$torrentName] Set file $pathInTorrent priority to $highestPriority" }
            }
        }

        private val _stats = object : DownloadStats() {
            override val totalBytes = MutableStateFlow(0L)
            override val progress = MutableStateFlow(0f)
            override val uploadRate get() = overallStatsImpl.uploadRate

            fun updateFrom(qb: QBFile) {
                totalBytes.value = qb.size
                progress.value = qb.progress
            }
        }

        init {
            // 同步下载进度
            scope.launch {
                while (isActive) {
                    try {
                        client.getTorrentFiles(hash = torrentInfo.hash, indexes = listOf(index)).firstOrNull()?.let {
                            _stats.updateFrom(it)
                        }

                        // 已经完成了, 就不需要更新了
                        if (_stats.progress.value == 1f) {
                            return@launch
                        }
                    } catch (e: Throwable) {
                        logger.error(e) { "Failed to update qBittorrent stats" }
                    }
                    delay(300)
                }

            }
        }

        override val stats: DownloadStats get() = _stats

        override val supportsStreaming: Boolean get() = false

        override fun createHandle(): TorrentFileHandle {
            val handle = FileHandleImpl()
            openHandles.add(handle)
            return handle
        }

        override fun createInput(): SeekableInput {
            val file = (resolveFileOrNull() ?: runBlocking { resolveDownloadingFile() })
            return file.toSeekableInput(
                onFillBuffer = {
                    // 不支持边下边播, 应等待下载完成
                    if (_stats.progress.value != 1f) {
                        runBlocking {
                            _stats.awaitFinished()
                        }
                    }
                }
            )
        }
    }

    override suspend fun getFiles(): List<TorrentFileEntry> = files.get()

    override fun closeIfNotInUse() {
        if (openHandles.isEmpty()) {
            close()
        }
    }

    @Volatile
    private var isClosed = false
    override fun close() {
        if (isClosed) return
        synchronized(this) {
            if (isClosed) return
            isClosed = true
        }

        scope.cancel()
    }
}
