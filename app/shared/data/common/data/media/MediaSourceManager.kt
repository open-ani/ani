package me.him188.ani.app.data.media

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import me.him188.ani.app.data.media.MediaCacheManager.Companion.LOCAL_FS_MEDIA_SOURCE_ID
import me.him188.ani.app.data.models.MediaSourceProxySettings
import me.him188.ani.app.data.models.ProxyAuthorization
import me.him188.ani.app.data.repositories.MediaSourceInstanceRepository
import me.him188.ani.app.data.repositories.MikanIndexCacheRepository
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.repositories.updateConfig
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.core.instance.MediaSourceInstance
import me.him188.ani.datasources.core.instance.MediaSourceSave
import me.him188.ani.datasources.dmhy.DmhyMediaSource
import me.him188.ani.datasources.mikan.MikanCNMediaSource
import me.him188.ani.datasources.mikan.MikanMediaSource
import me.him188.ani.datasources.mxdongman.MxdongmanMediaSource
import me.him188.ani.datasources.nyafun.NyafunMediaSource
import me.him188.ani.utils.coroutines.onReplacement
import me.him188.ani.utils.ktor.ClientProxyConfig
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.ServiceLoader
import java.util.UUID
import kotlin.coroutines.CoroutineContext

interface MediaSourceManager { // available by inject
    /**
     * 全部 [MediaSourceInstance] 列表.
     */
    val allInstances: Flow<List<MediaSourceInstance>>

    /**
     * 跟随设置更新的 [MediaSource] 列表. 已考虑代理和开关.
     */
    val enabledSources: Flow<List<MediaSource>>
        get() = allInstances.map { list ->
            list.filter { it.isEnabled }.map { it.source }
        }

    /**
     * 全部 [MediaSource] 列表. 已考虑代理和开关.
     */
    val allSources: Flow<List<MediaSource>>
        get() = allInstances.map { list -> list.map { it.source } }

    /**
     * 全部的 [MediaSource], 包括那些设置里关闭的, 包括本地的.
     */
    val allFactories: List<MediaSourceFactory>

    /**
     * 全部的 [MediaSource], 包括那些设置里关闭的, 包括本地的.
     */
    val allFactoryIds: List<String>

    /**
     * 全部的 [MediaSource], 包括那些设置里关闭的, 但不包括本地的.
     */
    val allFactoryIdsExceptLocal: List<String>
        get() = allFactoryIds.filter { !isLocal(it) }

    fun isLocal(mediaSourceId: String): Boolean {
        return mediaSourceId == LOCAL_FS_MEDIA_SOURCE_ID
    }

    fun instanceConfigFlow(instanceId: String): Flow<MediaSourceConfig>

    suspend fun addInstance(mediaSourceId: String, config: MediaSourceConfig)
    suspend fun updateConfig(instanceId: String, config: MediaSourceConfig)
    suspend fun setEnabled(instanceId: String, enabled: Boolean)
    suspend fun removeInstance(instanceId: String)
}

private inline val webMediaSourceIds
    get() = arrayOf(
        NyafunMediaSource.ID,
        MxdongmanMediaSource.ID
    )

private val defaultEnabledMediaSourceIds = if (Platform.currentPlatform.isDesktop())
    listOf(*webMediaSourceIds) // PC 默认不启用 BT 源
else listOf(
    *webMediaSourceIds,
    MikanCNMediaSource.ID,
    DmhyMediaSource.ID,
)

class MediaSourceManagerImpl(
    additionalSources: () -> List<MediaSource>, // local sources
    flowCoroutineContext: CoroutineContext = Dispatchers.Default,
) : MediaSourceManager, KoinComponent {
    private val settingsRepository: SettingsRepository by inject()
    private val proxyConfig = settingsRepository.proxySettings.flow
    private val mikanIndexCacheRepository: MikanIndexCacheRepository by inject()
    private val instances: MediaSourceInstanceRepository by inject()

    private val scope = CoroutineScope(CoroutineExceptionHandler { _, throwable ->
        // log error
        logger.error(throwable) { "DownloadProviderManager scope error" }
    })
    private val factories: List<MediaSourceFactory> = ServiceLoader.load(MediaSourceFactory::class.java).toList()

    private val additionalSources by lazy {
        additionalSources().map { source ->
            MediaSourceInstance(
                source.mediaSourceId,
                source.mediaSourceId,
                true,
                MediaSourceConfig.Default,
                source
            )
        }
    }
    override val allInstances =
        combine(instances.flow, proxyConfig.map { it.default }.distinctUntilChanged()) { saves, config ->
            // 一定要 additionalSources 在前面, local sources 需要优先使用
            this.additionalSources + saves.mapNotNull { createInstance(it, config) }
        }.onReplacement { list ->
            list.forEach { it.close() }
        }.flowOn(flowCoroutineContext).shareIn(scope, replay = 1, started = SharingStarted.Lazily)
    override val allFactories: List<MediaSourceFactory> get() = factories

    private fun createInstance(save: MediaSourceSave, config: MediaSourceProxySettings): MediaSourceInstance? {
        val factory = factories.find { it.mediaSourceId == save.mediaSourceId }
        return if (factory == null) {
            logger.error { "MediaSourceFactory not found for ${save.mediaSourceId}" }
            null
        } else {
            MediaSourceInstance(
                save.instanceId,
                save.mediaSourceId,
                save.isEnabled,
                save.config,
                factory.create(config, save.config)
            )
        }
    }

    override val allFactoryIds: List<String> by lazy {
        factories
            .map { it.mediaSourceId }
            .plus(this.additionalSources.map { it.mediaSourceId })
    }

    override fun instanceConfigFlow(instanceId: String): Flow<MediaSourceConfig> {
        return instances.flow.map { list ->
            list.find { it.instanceId == instanceId }?.config ?: MediaSourceConfig.Default
        }
    }

    override suspend fun addInstance(mediaSourceId: String, config: MediaSourceConfig) {
        val save = MediaSourceSave(
            instanceId = UUID.randomUUID().toString(),
            mediaSourceId = mediaSourceId,
            isEnabled = true,
            config = config,
        )
        instances.add(save)
    }

    override suspend fun updateConfig(instanceId: String, config: MediaSourceConfig) {
        instances.updateConfig(instanceId, config)
    }

    override suspend fun setEnabled(instanceId: String, enabled: Boolean) {
        instances.updateSave(instanceId) {
            copy(isEnabled = enabled)
        }
    }

    override suspend fun removeInstance(instanceId: String) {
        instances.remove(instanceId)
    }

    private fun MediaSourceFactory.create(
        globalProxySettings: MediaSourceProxySettings,
        config: MediaSourceConfig,
    ): MediaSource {
        val mediaSourceConfig = config.copy(
            proxy = config.proxy ?: globalProxySettings.toClientProxyConfig(),
            userAgent = getAniUserAgent(),
        )
        return when (this) {
            is MikanMediaSource.Factory -> create(mediaSourceConfig, mikanIndexCacheRepository)
            is MikanCNMediaSource.Factory -> create(mediaSourceConfig, mikanIndexCacheRepository)
            else -> create(mediaSourceConfig)
        }
    }

    private companion object {
        private val logger = logger<MediaSourceManager>()
    }
}

private fun MediaSourceProxySettings.toClientProxyConfig(): ClientProxyConfig? {
    return if (enabled) {
        config.run {
            ClientProxyConfig(
                url = url,
                authorization = authorization?.toHeader(),
            )
        }
    } else {
        null
    }
}

fun ProxyAuthorization.toHeader(): String = "Basic ${"$username:$password"}"
