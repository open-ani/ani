package me.him188.ani.app.tools.torrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import me.him188.ani.app.data.repositories.PreferencesRepository
import me.him188.ani.app.tools.torrent.engines.Libtorrent4jEngine
import me.him188.ani.app.tools.torrent.engines.QBittorrentEngine
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
    val libtorrent4j: Libtorrent4jEngine
    val qBittorrent: QBittorrentEngine

    val engines: List<TorrentEngine>
}

enum class TorrentEngineType(
    val id: String,
) {
    Libtorrent4j("libtorrent4j"),
    QBittorrent("qbittorrent"),
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
    private val preferencesRepository: PreferencesRepository by inject()

    private val libtorrent4jConfig get() = preferencesRepository.libtorrent4jConfig.flow
    private val qbittorrentConfig get() = preferencesRepository.qBittorrentConfig.flow

    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))

    override val libtorrent4j: Libtorrent4jEngine by lazy {
        Libtorrent4jEngine(scope, libtorrent4jConfig, saveDir(TorrentEngineType.Libtorrent4j))
    }

    override val qBittorrent: QBittorrentEngine by lazy {
        QBittorrentEngine(scope, qbittorrentConfig, saveDir(TorrentEngineType.QBittorrent))
    }

    override val engines: List<TorrentEngine> by lazy(LazyThreadSafetyMode.NONE) {
        listOf(libtorrent4j, qBittorrent)
    }
}
