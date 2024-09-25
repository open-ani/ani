/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.platform

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.him188.ani.app.data.bangumi.BangumiSubjectProvider
import me.him188.ani.app.data.models.map
import me.him188.ani.app.data.models.preference.configIfEnabledOrNull
import me.him188.ani.app.data.models.runApiRequest
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.models.subject.SubjectManagerImpl
import me.him188.ani.app.data.persistent.createDatabaseBuilder
import me.him188.ani.app.data.persistent.dataStores
import me.him188.ani.app.data.persistent.database.AniDatabase
import me.him188.ani.app.data.repository.BangumiCommentRepositoryImpl
import me.him188.ani.app.data.repository.BangumiEpisodeRepository
import me.him188.ani.app.data.repository.BangumiRelatedCharactersRepository
import me.him188.ani.app.data.repository.BangumiSubjectRepository
import me.him188.ani.app.data.repository.CommentRepository
import me.him188.ani.app.data.repository.DanmakuRegexFilterRepository
import me.him188.ani.app.data.repository.DanmakuRegexFilterRepositoryImpl
import me.him188.ani.app.data.repository.EpisodePlayHistoryRepository
import me.him188.ani.app.data.repository.EpisodePlayHistoryRepositoryImpl
import me.him188.ani.app.data.repository.EpisodePreferencesRepository
import me.him188.ani.app.data.repository.EpisodePreferencesRepositoryImpl
import me.him188.ani.app.data.repository.EpisodeRepositoryImpl
import me.him188.ani.app.data.repository.EpisodeScreenshotRepository
import me.him188.ani.app.data.repository.MediaSourceInstanceRepository
import me.him188.ani.app.data.repository.MediaSourceInstanceRepositoryImpl
import me.him188.ani.app.data.repository.MediaSourceSubscriptionRepository
import me.him188.ani.app.data.repository.MikanIndexCacheRepository
import me.him188.ani.app.data.repository.MikanIndexCacheRepositoryImpl
import me.him188.ani.app.data.repository.PreferencesRepositoryImpl
import me.him188.ani.app.data.repository.ProfileRepository
import me.him188.ani.app.data.repository.RemoteBangumiSubjectRepository
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.repository.SubjectSearchRepository
import me.him188.ani.app.data.repository.SubjectSearchRepositoryImpl
import me.him188.ani.app.data.repository.TokenRepository
import me.him188.ani.app.data.repository.TokenRepositoryImpl
import me.him188.ani.app.data.repository.UserRepository
import me.him188.ani.app.data.repository.UserRepositoryImpl
import me.him188.ani.app.data.repository.WhatslinkEpisodeScreenshotRepository
import me.him188.ani.app.data.source.AniAuthClient
import me.him188.ani.app.data.source.UpdateManager
import me.him188.ani.app.data.source.danmaku.DanmakuManager
import me.him188.ani.app.data.source.danmaku.DanmakuManagerImpl
import me.him188.ani.app.data.source.media.cache.DefaultMediaAutoCacheService
import me.him188.ani.app.data.source.media.cache.MediaAutoCacheService
import me.him188.ani.app.data.source.media.cache.MediaCacheManager
import me.him188.ani.app.data.source.media.cache.MediaCacheManagerImpl
import me.him188.ani.app.data.source.media.cache.createWithKoin
import me.him188.ani.app.data.source.media.cache.engine.DummyMediaCacheEngine
import me.him188.ani.app.data.source.media.cache.engine.TorrentMediaCacheEngine
import me.him188.ani.app.data.source.media.cache.storage.DirectoryMediaCacheStorage
import me.him188.ani.app.data.source.media.fetch.MediaSourceManager
import me.him188.ani.app.data.source.media.fetch.MediaSourceManagerImpl
import me.him188.ani.app.data.source.media.fetch.toClientProxyConfig
import me.him188.ani.app.data.source.media.source.codec.MediaSourceCodecManager
import me.him188.ani.app.data.source.media.source.subscription.MediaSourceSubscriptionUpdater
import me.him188.ani.app.data.source.media.source.subscription.SubscriptionUpdateData
import me.him188.ani.app.data.source.session.BangumiSessionManager
import me.him188.ani.app.data.source.session.OpaqueSession
import me.him188.ani.app.data.source.session.SessionManager
import me.him188.ani.app.data.source.session.unverifiedAccessToken
import me.him188.ani.app.tools.search.SubjectProvider
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.ui.subject.episode.video.TorrentMediaCacheProgressState
import me.him188.ani.app.videoplayer.torrent.TorrentVideoData
import me.him188.ani.app.videoplayer.ui.state.CacheProgressStateFactoryManager
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.DelegateBangumiClient
import me.him188.ani.datasources.bangumi.createBangumiClient
import me.him188.ani.utils.coroutines.childScope
import me.him188.ani.utils.coroutines.childScopeContext
import me.him188.ani.utils.coroutines.onReplacement
import me.him188.ani.utils.io.resolve
import me.him188.ani.utils.ktor.createDefaultHttpClient
import me.him188.ani.utils.ktor.proxy
import me.him188.ani.utils.ktor.registerLogging
import me.him188.ani.utils.ktor.userAgent
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.koin.core.KoinApplication
import org.koin.dsl.module
import kotlin.time.Duration.Companion.minutes

fun KoinApplication.getCommonKoinModule(getContext: () -> Context, coroutineScope: CoroutineScope) = module {
    // Repositories
    single<AniAuthClient> { AniAuthClient() }
    single<TokenRepository> { TokenRepositoryImpl(getContext().dataStores.tokenStore) }
    single<EpisodePreferencesRepository> { EpisodePreferencesRepositoryImpl(getContext().dataStores.preferredAllianceStore) }
    single<SessionManager> { BangumiSessionManager(koin, coroutineScope.coroutineContext) }
    single<BangumiClient> {
        val settings = get<SettingsRepository>()
        val sessionManager by inject<SessionManager>()
        DelegateBangumiClient(
            settings.proxySettings.flow.map { it.default }.map { proxySettings ->
                createBangumiClient(
                    @OptIn(OpaqueSession::class)
                    sessionManager.unverifiedAccessToken,
                    proxySettings.toClientProxyConfig(),
                    coroutineScope.coroutineContext,
                    userAgent = getAniUserAgent(currentAniBuildConfig.versionName),
                )
            }.onReplacement {
                it.close()
            }.shareIn(coroutineScope, started = SharingStarted.Lazily, replay = 1),
        )
    }
    single<SubjectProvider> { BangumiSubjectProvider(get<BangumiClient>()) }
    single<BangumiSubjectRepository> { RemoteBangumiSubjectRepository() }
    single<BangumiRelatedCharactersRepository> { BangumiRelatedCharactersRepository(get()) }
    single<EpisodeScreenshotRepository> { WhatslinkEpisodeScreenshotRepository() }
    single<SubjectManager> { SubjectManagerImpl(getContext().dataStores) }
    single<UserRepository> { UserRepositoryImpl() }
    single<CommentRepository> { BangumiCommentRepositoryImpl(get()) }
    single<BangumiEpisodeRepository> { EpisodeRepositoryImpl() }
    single<MediaSourceInstanceRepository> {
        MediaSourceInstanceRepositoryImpl(getContext().dataStores.mediaSourceSaveStore)
    }
    single<MediaSourceSubscriptionRepository> {
        MediaSourceSubscriptionRepository(getContext().dataStores.mediaSourceSubscriptionStore)
    }
    single<EpisodePlayHistoryRepository> {
        EpisodePlayHistoryRepositoryImpl(getContext().dataStores.episodeHistoryStore)
    }
    single<ProfileRepository> { ProfileRepository() }
    single<SubjectSearchRepository> {
        get<AniDatabase>().run { SubjectSearchRepositoryImpl(searchHistory(), searchTag()) }
    }

    single<DanmakuManager> {
        DanmakuManagerImpl(
            parentCoroutineContext = coroutineScope.coroutineContext,
        )
    }
    single<UpdateManager> {
        UpdateManager(
            saveDir = getContext().files.cacheDir.resolve("updates/download"),
        )
    }
    single<SettingsRepository> { PreferencesRepositoryImpl(getContext().dataStores.preferencesStore) }
    single<DanmakuRegexFilterRepository> { DanmakuRegexFilterRepositoryImpl(getContext().dataStores.danmakuFilterStore) }
    single<MikanIndexCacheRepository> { MikanIndexCacheRepositoryImpl(getContext().dataStores.mikanIndexStore) }

    single<AniDatabase> {
        getContext().createDatabaseBuilder()
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    // Media
    single<MediaCacheManager> {
        val id = MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID

        fun getMediaMetadataDir(engineId: String) = getContext().files.dataDir
            .resolve("media-cache").resolve(engineId)

        val engines = get<TorrentManager>().engines
        MediaCacheManagerImpl(
            storagesIncludingDisabled = buildList(capacity = engines.size) {
                if (currentAniBuildConfig.isDebug) {
                    // 注意, 这个必须要在第一个, 见 [DefaultTorrentManager.engines] 注释
                    add(
                        DirectoryMediaCacheStorage(
                            mediaSourceId = "test-in-memory",
                            metadataDir = getMediaMetadataDir("test-in-memory"),
                            engine = DummyMediaCacheEngine("test-in-memory"),
                            coroutineScope.childScopeContext(),
                        ),
                    )
                }
                for (engine in engines) {
                    add(
                        DirectoryMediaCacheStorage(
                            mediaSourceId = id,
                            metadataDir = getMediaMetadataDir(engine.type.id),
                            engine = TorrentMediaCacheEngine(
                                mediaSourceId = id,
                                torrentEngine = engine,
                            ),
                            coroutineScope.childScopeContext(),
                        ),
                    )
                }
            },
            backgroundScope = coroutineScope.childScope(),
        )
    }


    single<MediaSourceCodecManager> {
        MediaSourceCodecManager()
    }
    single<MediaSourceManager> {
        MediaSourceManagerImpl(
            additionalSources = {
                get<MediaCacheManager>().storagesIncludingDisabled.map { it.cacheMediaSource }
            },
        )
    }
    single<MediaSourceSubscriptionUpdater> {
        val settings = get<SettingsRepository>()
        val client = settings.proxySettings.flow.map { it.default }.map { proxySettings ->
            createDefaultHttpClient {
                userAgent(getAniUserAgent())
                proxy(proxySettings.configIfEnabledOrNull?.toClientProxyConfig())
            }.apply {
                registerLogging(logger<MediaSourceSubscriptionUpdater>())
            }
        }.onReplacement {
            it.close()
        }.shareIn(coroutineScope, started = SharingStarted.Lazily, replay = 1)
        MediaSourceSubscriptionUpdater(
            get<MediaSourceSubscriptionRepository>(),
            get<MediaSourceManager>(),
            get<MediaSourceCodecManager>(),
            requestSubscription = {
                client.first().runApiRequest { get(it.url) }.map { response ->
                    MediaSourceCodecManager.Companion.json.decodeFromString(
                        SubscriptionUpdateData.serializer(),
                        response.bodyAsText(),
                    )
                }
            },
        )
    }

    // Caching

    single<MediaAutoCacheService> {
        DefaultMediaAutoCacheService.createWithKoin()
    }

    CacheProgressStateFactoryManager.register(TorrentVideoData::class) { videoData, state ->
        TorrentMediaCacheProgressState(videoData.pieces) { state.value }
    }
}


/**
 * 会在非 preview 环境调用. 用来初始化一些模块
 */
fun KoinApplication.startCommonKoinModule(coroutineScope: CoroutineScope): KoinApplication {
    koin.get<MediaAutoCacheService>().startRegularCheck(coroutineScope)

    coroutineScope.launch {
        val manager = koin.get<MediaCacheManager>()
        for (storage in manager.storages) {
            storage.first()?.restorePersistedCaches()
        }
    }

    coroutineScope.launch {
        val subscriptionUpdater = koin.get<MediaSourceSubscriptionUpdater>()
        while (currentCoroutineContext().isActive) {
            val nextDelay = subscriptionUpdater.updateAllOutdated()
            delay(nextDelay.coerceAtLeast(1.minutes))
        }
    }

    coroutineScope.launch {
        // TODO: 这里是自动删除旧版数据源. 在未来 3.14 左右就可以去除这个了
        val removedFactoryIds = setOf("ntdm", "mxdongman", "nyafun")
        val manager = koin.get<MediaSourceManager>()
        for (instance in manager.allInstances.first()) {
            if (instance.factoryId.value in removedFactoryIds) {
                manager.removeInstance(instance.instanceId)
            }
        }
    }

    return this
}


fun createAppRootCoroutineScope(): CoroutineScope {
    val logger = logger("ani-root")
    return CoroutineScope(
        CoroutineExceptionHandler { coroutineContext, throwable ->
            logger.warn(throwable) {
                "Uncaught exception in coroutine $coroutineContext"
            }
        } + SupervisorJob() + Dispatchers.Default,
    )
}
