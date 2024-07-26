package me.him188.ani.app.tools.torrent

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.tools.torrent.engines.AnitorrentEngine
import me.him188.ani.utils.coroutines.childScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.coroutines.CoroutineContext

/**
 * 管理本地 BT 下载器的实现. 根据配置选择不同的下载器.
 *
 * 目前支持的下载实现:
 * - libtorrent4j (内嵌)
 * - qBittorrent (本机局域网).
 */
interface TorrentManager {
    val anitorrent: AnitorrentEngine

    val engines: List<TorrentEngine>
}

enum class TorrentEngineType(
    val id: String,
) {
    Anitorrent("anitorrent"),
}

class TorrentDownloaderInitializationException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

class TorrentDownloaderManagerError(
    val exception: Throwable,
)

class DefaultTorrentManager(
    parentCoroutineContext: CoroutineContext,
    private val saveDir: (type: TorrentEngineType) -> File,
) : TorrentManager, KoinComponent {
    private val settingsRepository: SettingsRepository by inject()

    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))

    override val anitorrent: AnitorrentEngine by lazy {
        AnitorrentEngine(
            scope.childScope(CoroutineName("AnitorrentEngine")),
            settingsRepository.anitorrentConfig.flow,
            settingsRepository.proxySettings.flow,
            saveDir(TorrentEngineType.Anitorrent),
        )
    }

    override val engines: List<TorrentEngine> by lazy(LazyThreadSafetyMode.NONE) {
        // 注意, 是故意只启用一个下载器的, 因为每个下载器都会创建一个 DirectoryMediaCacheStorage
        // 并且使用相同的 mediaSourceId: MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID.
        // 搜索数据源时会使用 mediaSourceId 作为 map key, 导致总是只会用一个 storage.
        // 
        // 如果要支持多个, 需要考虑将所有 storage 合并成一个 MediaSource.

        when (Platform.currentPlatform) {
            is Platform.Desktop -> listOf(anitorrent)
            Platform.Android -> listOf(anitorrent)
        }
    }
}
