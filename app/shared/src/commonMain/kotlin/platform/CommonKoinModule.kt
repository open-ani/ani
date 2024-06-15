/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.platform

import androidx.compose.runtime.Stable
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.client.plugins.UserAgent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.him188.ani.app.data.danmaku.DanmakuManager
import me.him188.ani.app.data.danmaku.DanmakuManagerImpl
import me.him188.ani.app.data.database.AniDatabase
import me.him188.ani.app.data.media.DefaultMediaAutoCacheService
import me.him188.ani.app.data.media.DummyMediaCacheEngine
import me.him188.ani.app.data.media.MediaAutoCacheService
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.media.MediaCacheManagerImpl
import me.him188.ani.app.data.media.MediaSourceManager
import me.him188.ani.app.data.media.MediaSourceManagerImpl
import me.him188.ani.app.data.media.TorrentMediaCacheEngine
import me.him188.ani.app.data.repositories.EpisodePreferencesRepository
import me.him188.ani.app.data.repositories.EpisodePreferencesRepositoryImpl
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.data.repositories.EpisodeRepositoryImpl
import me.him188.ani.app.data.repositories.EpisodeRevisionRepository
import me.him188.ani.app.data.repositories.EpisodeRevisionRepositoryImpl
import me.him188.ani.app.data.repositories.MediaSourceInstanceRepository
import me.him188.ani.app.data.repositories.MediaSourceInstanceRepositoryImpl
import me.him188.ani.app.data.repositories.MediaSourceSaves
import me.him188.ani.app.data.repositories.MikanIndexCacheRepository
import me.him188.ani.app.data.repositories.MikanIndexCacheRepositoryImpl
import me.him188.ani.app.data.repositories.PreferencesRepositoryImpl
import me.him188.ani.app.data.repositories.ProfileRepository
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.repositories.SubjectRepository
import me.him188.ani.app.data.repositories.SubjectRepositoryImpl
import me.him188.ani.app.data.repositories.SubjectSearchRepository
import me.him188.ani.app.data.repositories.SubjectSearchRepositoryImpl
import me.him188.ani.app.data.repositories.TokenRepository
import me.him188.ani.app.data.repositories.TokenRepositoryImpl
import me.him188.ani.app.data.repositories.UserRepository
import me.him188.ani.app.data.repositories.UserRepositoryImpl
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.data.subject.SubjectManagerImpl
import me.him188.ani.app.data.update.UpdateManager
import me.him188.ani.app.persistent.createDatabaseBuilder
import me.him188.ani.app.persistent.dataStores
import me.him188.ani.app.persistent.preferencesStore
import me.him188.ani.app.persistent.preferredAllianceStore
import me.him188.ani.app.persistent.tokenStore
import me.him188.ani.app.platform.Platform.Companion.currentPlatform
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.session.SessionManagerImpl
import me.him188.ani.app.tools.torrent.TorrentEngineType
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.subject.SubjectProvider
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.BangumiSubjectProvider
import me.him188.ani.datasources.core.cache.DirectoryMediaCacheStorage
import me.him188.ani.datasources.core.instance.MediaSourceSave
import me.him188.ani.utils.coroutines.childScope
import me.him188.ani.utils.coroutines.childScopeContext
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.koin.core.KoinApplication
import org.koin.dsl.module
import java.util.UUID

@Suppress("UnusedReceiverParameter") // bug
fun KoinApplication.getCommonKoinModule(getContext: () -> Context, coroutineScope: CoroutineScope) = module {
    // Repositories
    single<TokenRepository> { TokenRepositoryImpl(getContext().tokenStore) }
    single<EpisodePreferencesRepository> { EpisodePreferencesRepositoryImpl(getContext().preferredAllianceStore) }
    single<SessionManager> { SessionManagerImpl() }
    single<BangumiClient> { createBangumiClient() }
    single<SubjectProvider> { BangumiSubjectProvider(get<BangumiClient>()) }
    single<SubjectRepository> { SubjectRepositoryImpl() }
    single<SubjectManager> { SubjectManagerImpl(getContext()) }
    single<UserRepository> { UserRepositoryImpl() }
    single<EpisodeRevisionRepository> { EpisodeRevisionRepositoryImpl() }
    single<EpisodeRepository> { EpisodeRepositoryImpl() }
    single<MediaSourceInstanceRepository> {
        MediaSourceInstanceRepositoryImpl(getContext().dataStores.mediaSourceSaveStore)
    }
    single<ProfileRepository> { ProfileRepository() }
    single<SubjectSearchRepository> {
        get<AniDatabase>().run { SubjectSearchRepositoryImpl(searchHistory(), searchTag()) }
    }
    
    single<DanmakuManager> {
        DanmakuManagerImpl(
            parentCoroutineContext = coroutineScope.coroutineContext
        )
    }
    single<UpdateManager> {
        UpdateManager(
            saveDir = getContext().files.cacheDir.resolve("updates/download"),
        )
    }
    single<SettingsRepository> { PreferencesRepositoryImpl(getContext().preferencesStore) }
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

        // migrate old files
        getContext().files.cacheDir.resolve("media").let { oldDir ->
            // 旧的都是 libtorrent4j
            if (oldDir.exists()) {
                oldDir.copyRecursively(
                    getMediaMetadataDir(TorrentEngineType.Libtorrent4j.id), true
                )
                oldDir.deleteRecursively()
            }
        }

        val engines = get<TorrentManager>().engines
        MediaCacheManagerImpl(
            storagesIncludingDisabled = buildList(capacity = engines.size) {
                if (DummyMediaCacheEngine.isEnabled) {
                    // 注意, 这个必须要在第一个, 见 [DefaultTorrentManager.engines] 注释
                    add(
                        DirectoryMediaCacheStorage(
                            mediaSourceId = "test-in-memory",
                            metadataDir = getMediaMetadataDir("test-in-memory").toPath(),
                            engine = DummyMediaCacheEngine("test-in-memory"),
                            coroutineScope.childScopeContext(),
                        )
                    )
                }
                for (engine in engines) {
                    add(
                        DirectoryMediaCacheStorage(
                            mediaSourceId = id,
                            metadataDir = getMediaMetadataDir(engine.type.id).toPath(),
                            engine = TorrentMediaCacheEngine(
                                mediaSourceId = id,
                                torrentEngine = engine,
                            ),
                            coroutineScope.childScopeContext(),
                        )
                    )
                }
            },
            backgroundScope = coroutineScope.childScope(),
        )
    }


    single<MediaSourceManager> {
        MediaSourceManagerImpl(
            additionalSources = {
                get<MediaCacheManager>().storagesIncludingDisabled.map { it.cacheMediaSource }
            }
        )
    }

    // Caching

    single<MediaAutoCacheService> {
        DefaultMediaAutoCacheService()
    }
}


/**
 * 会在非 preview 环境调用. 用来初始化一些模块
 */
fun KoinApplication.startCommonKoinModule(coroutineScope: CoroutineScope): KoinApplication {
    koin.get<MediaAutoCacheService>().startRegularCheck(coroutineScope)


    coroutineScope.launch {
        // 迁移旧的 fallbackMediaSourceIds 到 MediaSourceInstance
        val mediaSourceInstanceRepository = koin.get<MediaSourceInstanceRepository>()
        val settingsRepository = koin.get<SettingsRepository>()
        val mediaSourceManager = koin.get<MediaSourceManager>()
        val mediaPreference = settingsRepository.defaultMediaPreference.flow.first()

        val logger = logger("media-source-migration")

        @Suppress("DEPRECATION")
        val legacyIds = mediaPreference.fallbackMediaSourceIds
        if (!legacyIds.isNullOrEmpty()) {
            // 需要迁移
            mediaSourceInstanceRepository.clear()
            for (id in legacyIds) {
                logger.info { "Migrating legacy media source $id" }
                if (mediaSourceManager.isLocal(id)) {
                    logger.info { "Migrating legacy media source $id: ignoring local" }
                }
                mediaSourceInstanceRepository.add(
                    mediaSourceSave = MediaSourceSave(
                        instanceId = UUID.randomUUID().toString(),
                        mediaSourceId = id,
                        isEnabled = true,
                        config = MediaSourceConfig.Default,
                    )
                )
            }
            // 把没启用的也添加上, 符合旧逻辑
            for (instance in MediaSourceSaves.Default.instances) {
                if (legacyIds.contains(instance.mediaSourceId)) {
                    continue
                }
                mediaSourceInstanceRepository.add(
                    mediaSourceSave = instance.copy(isEnabled = false)
                )
            }
            settingsRepository.defaultMediaPreference.set(
                mediaPreference.copy(fallbackMediaSourceIds = null)
            )
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

@Stable
interface AniBuildConfig {
    /**
     * `3.0.0-rc04`
     */
    val versionName: String
    val bangumiOauthClientAppId: String
    val bangumiOauthClientSecret: String
    val isDebug: Boolean

    companion object {
        @Stable
        fun current(): AniBuildConfig = currentAniBuildConfig
    }
}

/**
 * E.g. `30000` for `3.0.0`, `30102` for `3.1.2`
 */
val AniBuildConfig.versionCode: String
    get() = buildString {
        val split = versionName.substringBefore("-").split(".")
        if (split.size == 3) {
            split[0].toIntOrNull()?.let {
                append(it.toString())
            }
            split[1].toIntOrNull()?.let {
                append(it.toString().padStart(2, '0'))
            }
            split[2].toIntOrNull()?.let {
                append(it.toString().padStart(2, '0'))
            }
        } else {
            for (section in split) {
                section.toIntOrNull()?.let {
                    append(it.toString().padStart(2, '0'))
                }
            }
        }
    }

@Stable
expect val currentAniBuildConfigImpl: AniBuildConfig

@Stable
inline val currentAniBuildConfig: AniBuildConfig get() = currentAniBuildConfigImpl

/**
 * 满足各个数据源建议格式的 User-Agent, 所有 HTTP 请求都应该带此 UA.
 */
fun getAniUserAgent(
    version: String = currentAniBuildConfig.versionName,
    platform: String = currentPlatform.nameAndArch,
): String = "open-ani/ani/$version ($platform) (https://github.com/open-ani/ani)"

fun createBangumiClient(): BangumiClient {
    return BangumiClient.create(
        currentAniBuildConfig.bangumiOauthClientAppId,
        currentAniBuildConfig.bangumiOauthClientSecret
    ) {
        install(UserAgent) {
            agent = getAniUserAgent(currentAniBuildConfig.versionName)
        }
    }
}