package me.him188.ani.app.tools.torrent

import androidx.annotation.CallSuper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.utils.coroutines.onReplacement
import me.him188.ani.utils.coroutines.runUntilSuccess
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn


/**
 * libtorrent4j 或 qBittorrent
 */
interface TorrentEngine {
    val type: TorrentEngineType

    /**
     * 该实现的位置, 用于标识不同的下载器.
     */
    val location: MediaSourceLocation

    /**
     * 是否被当前平台支持
     */
    val isSupported: Flow<Boolean>

    /**
     * 根据用户偏好设置, 此实现是否启用.
     */
    val isEnabled: Flow<Boolean>

    suspend fun testConnection(): Boolean

    /**
     * 获取已经创建好的下载器, 或者创建一个下载器.
     *
     * 本函数支持协程取消. 当协程取消时, 创建工作会延迟一段时间后才会停止, 以抵消重复的创建和销毁.
     *
     * @throws IllegalStateException 当 [isEnabled] 为 `false` 时抛出
     * @throws TorrentDownloaderInitializationException 当创建失败时抛出
     */
    @Throws(TorrentDownloaderInitializationException::class)
    suspend fun getDownloader(): TorrentDownloader?
}

interface TorrentEngineConfig {
    val enabled: Boolean
}

abstract class AbstractTorrentEngine<Downloader : TorrentDownloader, Config : TorrentEngineConfig>(
    protected val scope: CoroutineScope,
    final override val type: TorrentEngineType,
    protected val config: Flow<Config>,
) : TorrentEngine {
    protected val logger = logger(this::class)

    val lastError: MutableStateFlow<TorrentDownloaderManagerError?> = MutableStateFlow(null)

    private val downloader = config
        .debounce(1000)
        .map { config ->
            runUntilSuccess(
                3,
                onFailure = { e ->
                    lastError.value = TorrentDownloaderManagerError(e)
                    logger.warn(e) { "Failed to create TorrentDownloader $type, retrying later" }
                }
            ) {
                newInstance(config)
            }.also { downloader ->
                scope.coroutineContext.job.invokeOnCompletion {
                    downloader.close()
                }
            }
        }
        .onReplacement {
            closeInstance(it)
        }
        .shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)

    protected abstract suspend fun newInstance(config: Config): Downloader

    @CallSuper
    protected open fun closeInstance(downloader: Downloader) {
        downloader.close()
    }

    final override suspend fun getDownloader(): Downloader {
        if (!isEnabled.first()) {
            throw IllegalStateException("Implementation is not enabled")
        }
        return try {
            downloader.first()
        } catch (e: Throwable) {
            throw TorrentDownloaderInitializationException(cause = e)
        }
    }
}
