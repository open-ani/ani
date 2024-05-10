package me.him188.ani.app.torrent.qbittorrent

import androidx.compose.ui.util.fastForEachIndexed
import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
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
import kotlinx.coroutines.withTimeout
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.torrent.api.HttpFileDownloader
import me.him188.ani.app.torrent.api.TorrentDownloadSession
import me.him188.ani.app.torrent.api.TorrentDownloadState
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentLibInfo
import me.him188.ani.app.torrent.api.files.AbstractTorrentFileEntry
import me.him188.ani.app.torrent.api.files.DownloadStats
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import me.him188.ani.app.torrent.api.files.FilePriority
import me.him188.ani.app.torrent.api.files.PieceState
import me.him188.ani.app.torrent.api.files.TorrentFileEntry
import me.him188.ani.app.torrent.api.files.TorrentFileHandle
import me.him188.ani.app.torrent.api.files.TorrentFilePieceMatcher
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.io.TorrentFileIO
import me.him188.ani.app.torrent.io.TorrentInput
import me.him188.ani.utils.coroutines.SuspendLazy
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.toSeekableInput
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.math.absoluteValue


/**
 * 连接 qBittorrent API 的下载器.
 *
 * 下载依靠 QB 的分类 (category) 来区分.
 *
 * - 在解析磁力链 [fetchTorrent] 时, 使用 "Ani-temp" 分类, 添加种子拿到磁力链后立即删除.
 * - 在下载时, 使用 "Ani" 分类.
 */
class QBittorrentTorrentDownloader(
    config: QBittorrentClientConfig,
    private val saveDir: File,
    private val httpFileDownloader: HttpFileDownloader,
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
                    logger.error(e) { "Failed to update qBittorrent stats for global info" }
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

    private fun decodeTorrentInfoFromTorrentFile(
        data: ByteArray,
    ): Map<String, Any>? {
        val bencode: Map<String, Any> = Bencode().run {
            decode(data, Type.DICTIONARY)
        }
        val info = bencode["info"] ?: return null
        @Suppress("UNCHECKED_CAST")
        return info as? Map<*, *> as Map<String, Any>? ?: return null
        // 经测试, 这边计算的 hash 总是和 qb 的不同, 就直接用名字匹配吧
//        if (info is ) {
//            return TorrentFileIO.hashSha1(Bencode().encode(info))
//        }
//        return null
    }

    override suspend fun fetchTorrent(uri: String, timeoutSeconds: Int): EncodedTorrentInfo {
        // 从下载分类 "Ani" 中找有没有正在下载的. 这只能匹配 magnet. QB 不会保存来源 HTTPS 种子文件信息.
        val torrentList = client.getTorrentList()
        if (uri.startsWith("https://") || uri.startsWith("http://")) {
            val data = httpFileDownloader.download(uri)
            val name = decodeTorrentInfoFromTorrentFile(data)?.get("name")?.toString()
            if (name != null) {
                torrentList.firstOrNull { it.name == name }?.let {
                    logger.info { "Matched torrent using name '$name'. hash=${it.hash}" }
                    return EncodedTorrentInfo(it.magnetUri.toByteArray())
                }
            }
        }

        // Find existing
        torrentList.firstOrNull { it.magnetUri == uri }?.let {
            logger.info { "Matched torrent using magnetUri. hash=${it.hash}" }
            return EncodedTorrentInfo(it.magnetUri.toByteArray())
        }

        val tempDir = saveDir.resolve(uri.hashCode().absoluteValue.toString()).apply {
            mkdirs()
        }

        logger.info { "Did not match existing, starting download" }
        try {
            client.addTorrentFromUri(
                uri, savePath = tempDir.absolutePath, paused = true,
            )
            val actual = withTimeout(timeoutSeconds * 1000L) {
                awaitTorrentData { it.savePath == tempDir.absolutePath }
            }
            client.deleteTorrents(listOf(actual.hash), false)
            return EncodedTorrentInfo(actual.magnetUri.toByteArray())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private suspend fun awaitTorrentData(
        category: String? = null,
        predicate: (QBTorrent) -> Boolean
    ): QBTorrent {
        while (true) {
            val list = if (category == null) {
                client.getTorrentList()
            } else {
                client.getTorrentList(category = category)
            }
            val torrent = list.firstOrNull(predicate)
            if (torrent != null) {
                return torrent
            }
            logger.info { "Waiting for torrent data..." }
            delay(1000)
        }
    }

    override suspend fun startDownload(
        data: EncodedTorrentInfo,
        parentCoroutineContext: CoroutineContext
    ): TorrentDownloadSession {
        // note: this hash is not equivalent to torrent info hash
        val dir = getSaveDirForTorrent(data).apply { mkdirs() }

        val info = client.getTorrentList().firstOrNull { it.savePath == dir.absolutePath }
            ?: run {
                val uri = data.data.decodeToString()
                check(uri.startsWith("magnet")) { "Only magnet uri is supported, but had $uri" }
                client.addTorrentFromUri(
                    uri, // magnet
                    savePath = dir.absolutePath,
                    paused = false,
                    sequentialDownload = true,
                    firstLastPiecePrio = true,
                )
                awaitTorrentData { it.savePath == dir.absolutePath }
            }

        client.recheckTorrents(listOf(info.hash))

        return QBittorrentTorrentDownloadSession(
            torrentInfo = info,
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

        scope.cancel()
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
                    logger.error(e) { "Failed to update qBittorrent stats for session info" }
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

        val torrentProperties = client.getTorrentProperties(torrentInfo.hash)
        val allPieces = Piece.buildPieces(
            totalSize = torrentInfo.totalSize,
            pieceSize = torrentProperties.pieceSize
        )

        val numFiles = files.size

        var currentOffset = 0L
        val list = List(numFiles) { index ->
            val file = files[index]
            val size = file.size
            FileEntryImpl(
                index = file.index,
                offset = currentOffset,
                length = file.size,
                saveDirectory = saveDirectory,
                relativePath = file.name,
                torrentName = torrentInfo.name,
                isDebug = false,
                parentCoroutineContext = parentCoroutineContext,
                pieces = TorrentFilePieceMatcher.matchPiecesForFile(allPieces, currentOffset, file.size)
//                pieces = Piece.buildPieces(
//                    file.pieceRange.last - file.pieceRange.first + 1,
//                    initial = currentOffset,
//                    getPieceSize = { _ ->
//                        torrentProperties.pieceSize
//                    }
//                )
            ).also {
                currentOffset += size
            }
        }
        list
    }

    private val openHandles = ConcurrentLinkedQueue<FileEntryImpl.FileHandleImpl>()

    private inner class FileEntryImpl(
        index: Int,
        private val offset: Long,
        length: Long,
        saveDirectory: File,
        relativePath: String,
        torrentName: String,
        isDebug: Boolean,
        parentCoroutineContext: CoroutineContext,
        override val pieces: List<Piece>?,
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

                        if (pieces != null) {
                            client.getPieceStates(torrentInfo.hash).fastForEachIndexed { i, qbPieceState ->
                                val newState = when (qbPieceState) {
                                    QBPieceState.NOT_DOWNLOADED -> PieceState.READY
                                    QBPieceState.DOWNLOADING -> PieceState.DOWNLOADING
                                    QBPieceState.DOWNLOADED -> PieceState.FINISHED
                                }
                                if (newState == PieceState.FINISHED && pieces[i].state.value != newState) {
                                    logger.info { "[$torrentName] Piece ${pieces[i].pieceIndex} finished" }
                                }
                                pieces[i].state.value = newState
                            }
                        }

                        // 已经完成了, 就不需要更新了
                        if (_stats.progress.value == 1f) {
                            logger.info { "[$torrentName] Torrent finished" }
                            pieces?.forEach { it.state.value = PieceState.FINISHED }
                            return@launch
                        }
                    } catch (e: Throwable) {
                        logger.error(e) { "Failed to update qBittorrent stats for entry" }
                    }
                    delay(900)
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
            val file = resolveFileOrNull()
                ?: runBlocking { resolveDownloadingFile() } // don't switch thread if we don't need to
            if (pieces != null) {
                logger.info { "Using TorrentInput since we have pieces. pieces=$pieces, file offset=${offset}, file length=$length," }
                return TorrentInput(
                    RandomAccessFile(file, "r"),
                    pieces,
                    logicalStartOffset = offset,
                    onWait = { piece ->
                        logger.info { "[TorrentDownloadControl] $torrentName: Set piece ${piece.pieceIndex} deadline to 0 because it was requested " }
                        // TODO: QB 优先级
//                        torrentThreadTasks.submit { handle ->
//                            handle.setPieceDeadline(piece.pieceIndex, 0) // 最高优先级
//                            for (i in (piece.pieceIndex + 1..piece.pieceIndex + 3)) {
//                                if (i < pieces.size - 1) {
//                                    handle.setPieceDeadline( // 按请求时间的优先
//                                        i,
//                                        calculatePieceDeadlineByTime(i)
//                                    )
//                                }
//                            }
//                        }
                    },
                    size = length
                )
            }
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
