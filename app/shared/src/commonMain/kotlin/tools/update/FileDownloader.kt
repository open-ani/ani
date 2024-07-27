package me.him188.ani.app.tools.update

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.prepareRequest
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.utils.coroutines.cancellableCoroutineScope
import me.him188.ani.utils.coroutines.withExceptionCollector
import me.him188.ani.utils.io.DigestAlgorithm
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.absolutePath
import me.him188.ani.utils.io.bufferedSink
import me.him188.ani.utils.io.bufferedSource
import me.him188.ani.utils.io.delete
import me.him188.ani.utils.io.exists
import me.him188.ani.utils.io.length
import me.him188.ani.utils.io.readAndDigest
import me.him188.ani.utils.io.readText
import me.him188.ani.utils.io.resolve
import me.him188.ani.utils.io.writeText
import me.him188.ani.utils.ktor.createDefaultHttpClient
import me.him188.ani.utils.ktor.userAgent
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import kotlin.time.Duration.Companion.seconds

/**
 * 文件下载器.
 *
 * - 支持从多个 URL 下载文件, 并保存到指定目录.
 * - 支持检查已经下载好的文件.
 * - 支持提供进度.
 */
interface FileDownloader {
    /**
     * Range: `[0, 1]`
     */
    val progress: Flow<Float>
    val state: StateFlow<FileDownloaderState>

    /**
     * 开始在后台下载文件, 并保存到 [saveDir]. 同时会保存一个 sha256 校验和文件 `${filename}.sha256`.
     * 将会依次重试, 直到第一个成功
     *
     * 同时只会有一个 [download] 运行. 若已经有一个, 此函数立即返回 `false`. 否则返回 `true`.
     */
    suspend fun download(
        alternativeUrls: List<String>,
        filenameProvider: (url: String) -> String,
        saveDir: SystemPath,
    ): Boolean
}

sealed class FileDownloaderState {
    /**
     * [FileDownloader.download] 还没被调用过
     */
    data object Idle : FileDownloaderState()

    /**
     * 正在尝试下载某一个 URL
     */
    data object Downloading : FileDownloaderState()

    sealed class Completed : FileDownloaderState()

    /**
     * 成功下载并保存了校验和文件
     */
    data class Succeed(
        /**
         * 原始来源 URL
         */
        val url: String,
        /**
         * 下载完成的文件
         */
        val file: SystemPath,
        // 校验和文件总是 `${file.name}.sha256`
    ) : Completed()

    data class Failed(val throwable: Throwable) : Completed()
}

class DefaultFileDownloader : FileDownloader {
    private companion object {
        private val logger = logger<DefaultFileDownloader>()
    }

    override val state = MutableStateFlow<FileDownloaderState>(FileDownloaderState.Idle)

    private val _progress = MutableStateFlow(0f)
    override val progress get() = _progress

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun download(
        alternativeUrls: List<String>,
        filenameProvider: (url: String) -> String,
        saveDir: SystemPath,
    ): Boolean {
        require(alternativeUrls.isNotEmpty()) { "alternatives must not be empty" }
        state.update {
            if (it != FileDownloaderState.Idle && it !is FileDownloaderState.Completed) {
                return false
            }
            FileDownloaderState.Downloading
        }
        createDefaultHttpClient {
            userAgent(getAniUserAgent())
            expectSuccess = true
            install(HttpTimeout) {
                requestTimeoutMillis = 1000_000
            }
        }.use { client ->
            _progress.value = 0f
            withExceptionCollector {
                for (url in alternativeUrls) { // 依次尝试
                    state.value = FileDownloaderState.Downloading
                    try {
                        val filename = filenameProvider(url)
                        val targetFile = saveDir.resolve(filename)
                        val checksumFile = saveDir.resolve("$filename.sha256")
                        if (targetFile.exists() && checksumFile.exists()) {
                            logger.info { "File $filename already exists, size=${targetFile.length().bytes}, checking checksum" }
                            val checksum = checksumFile.readText()
                            val actualChecksum = targetFile.bufferedSource().use {
                                it.readAndDigest(DigestAlgorithm.SHA256).toHexString()
                            }
                            if (checksum == actualChecksum) {
                                logger.info { "File $filename already exists and checksum matches, skipping download" }
                                state.value = FileDownloaderState.Succeed(url, targetFile)
                                return true
                            } else {
                                logger.info { "File $filename already exists, but checksum does not match, deleting" }
                                withContext(Dispatchers.IO) {
                                    targetFile.delete()
                                    checksumFile.delete()
                                }
                            }
                        }
                        tryDownload(
                            client,
                            url,
                            targetFile,
                        )
                        // 下载完成, 更新 checksum
                        checksumFile.writeText(
                            targetFile.bufferedSource().use {
                                it.readAndDigest(DigestAlgorithm.SHA256).toHexString()
                            },
                        )
                        state.value = FileDownloaderState.Succeed(url, targetFile)
                        return true
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        collect(e)
                        state.value = FileDownloaderState.Failed(getLast()!!)
                        continue
                    }
                }
                throwLast()
            }
        }
    }

    /**
     * 尝试下载一个文件并保存校验和, 同时会更新进度 [_progress], 不会更新状态. 下载失败时抛出异常.
     */
    private suspend fun tryDownload(
        client: HttpClient,
        url: String,
        file: SystemPath,
    ) {
        cancellableCoroutineScope {
            logger.info { "Attempting $url" }
            try {
                client.prepareRequest(url).execute { resp ->
                    val length = resp.contentLength()
                    logger.info { "Downloading $url to ${file.absolutePath}, length=${(length ?: 0).bytes}" }
                    val downloaded = java.util.concurrent.atomic.AtomicLong(0L)
                    val input = resp.bodyAsChannel()
                    val buffer = ByteArray(8192)
                    if (length != null) {
                        launch {
                            while (isActive) {
                                delay(1.seconds)
                                _progress.value = downloaded.get().toFloat() / length
                            }
                        }
                    }
                    file.bufferedSink().use { output ->
                        while (!input.isClosedForRead) {
                            val read = input.readAvailable(buffer, 0, buffer.size)
                            if (read == -1) {
                                return@execute
                            }
                            downloaded.addAndGet(read.toLong())
                            withContext(Dispatchers.IO) {
                                output.write(buffer, 0, read)
                            }
                        }
                    }
                    logger.info { "Successfully downloaded: $url" }
                }
            } catch (e: Throwable) {
                logger.info(e) { "Failed to download $url" }
                throw e
            } finally {
                cancelScope()
            }
        }
    }
}
