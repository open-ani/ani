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
import io.ktor.client.plugins.UserAgent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.him188.ani.app.data.media.DefaultMediaAutoCacheService
import me.him188.ani.app.data.media.MediaAutoCacheService
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.media.MediaCacheManagerImpl
import me.him188.ani.app.data.media.MediaSourceManager
import me.him188.ani.app.data.media.MediaSourceManagerImpl
import me.him188.ani.app.data.media.TorrentMediaCacheEngine
import me.him188.ani.app.data.media.resolver.LocalFileVideoSourceResolver
import me.him188.ani.app.data.media.resolver.TorrentVideoSourceResolver
import me.him188.ani.app.data.media.resolver.VideoSourceResolver
import me.him188.ani.app.data.repositories.EpisodePreferencesRepository
import me.him188.ani.app.data.repositories.EpisodePreferencesRepositoryImpl
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.data.repositories.EpisodeRepositoryImpl
import me.him188.ani.app.data.repositories.EpisodeRevisionRepository
import me.him188.ani.app.data.repositories.EpisodeRevisionRepositoryImpl
import me.him188.ani.app.data.repositories.PreferencesRepository
import me.him188.ani.app.data.repositories.PreferencesRepositoryImpl
import me.him188.ani.app.data.repositories.ProfileRepository
import me.him188.ani.app.data.repositories.SubjectRepository
import me.him188.ani.app.data.repositories.SubjectRepositoryImpl
import me.him188.ani.app.data.repositories.TokenRepository
import me.him188.ani.app.data.repositories.TokenRepositoryImpl
import me.him188.ani.app.data.repositories.UserRepository
import me.him188.ani.app.data.repositories.UserRepositoryImpl
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.data.subject.SubjectManagerImpl
import me.him188.ani.app.persistent.preferencesStore
import me.him188.ani.app.persistent.preferredAllianceStore
import me.him188.ani.app.persistent.tokenStore
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.session.SessionManagerImpl
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.danmaku.api.DanmakuProvider
import me.him188.ani.danmaku.dandanplay.DandanplayClient
import me.him188.ani.danmaku.dandanplay.DandanplayDanmakuProvider
import me.him188.ani.datasources.api.subject.SubjectProvider
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.BangumiSubjectProvider
import me.him188.ani.datasources.core.cache.DirectoryMediaCacheStorage
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.koin.core.KoinApplication
import org.koin.dsl.module

@Suppress("UnusedReceiverParameter") // bug
fun KoinApplication.getCommonKoinModule(getContext: () -> Context, coroutineScope: CoroutineScope) = module {
    // Repositories
    single<TokenRepository> { TokenRepositoryImpl(getContext().tokenStore) }
    single<EpisodePreferencesRepository> { EpisodePreferencesRepositoryImpl(getContext().preferredAllianceStore) }
    single<SessionManager> { SessionManagerImpl() }
    single<BangumiClient> { createBangumiClient() }
    single<SubjectProvider> { BangumiSubjectProvider(get<BangumiClient>()) }
    single<SubjectRepository> { SubjectRepositoryImpl() }
    single<SubjectManager> { SubjectManagerImpl() }
    single<UserRepository> { UserRepositoryImpl() }
    single<EpisodeRevisionRepository> { EpisodeRevisionRepositoryImpl() }
    single<EpisodeRepository> { EpisodeRepositoryImpl() }
    single<ProfileRepository> { ProfileRepository() }
    single<DanmakuProvider> {
        DandanplayDanmakuProvider(dandanplayClient = DandanplayClient {
            install(UserAgent) {
                agent = getAniUserAgent(currentAniBuildConfig.versionName)
            }
        })
    }
    single<PreferencesRepository> { PreferencesRepositoryImpl(getContext().preferencesStore) }

    // Media
    single<MediaCacheManager> {
        val id = MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID
        MediaCacheManagerImpl(
            listOf(
                DirectoryMediaCacheStorage(
                    id,
                    getContext().files.cacheDir.resolve("media").toPath(),
                    TorrentMediaCacheEngine(
                        id,
                        getTorrentDownloader = { get<TorrentManager>().downloader.await() },
                    ),
                    coroutineScope.coroutineContext
                )
            )
        )
    }


    single<VideoSourceResolver> {
        VideoSourceResolver.from(
            TorrentVideoSourceResolver(get()),
            LocalFileVideoSourceResolver(),
        )
    }
    single<MediaSourceManager> {
        MediaSourceManagerImpl(
            additionalSources = {
                get<MediaCacheManager>().storages.map { it.cacheMediaSource }
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
    coroutineScope.launch(Dispatchers.IO) {
        koin.get<MediaCacheManager>().storages // initialize caches as the storage constructors needs to do IO 
    }
    koin.get<MediaAutoCacheService>().startRegularCheck(coroutineScope)
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
expect val currentAniBuildConfig: AniBuildConfig

fun getAniUserAgent(
    version: String = currentAniBuildConfig.versionName,
    platform: String = Platform.currentPlatform().name,
): String {
    return "him188/ani/$version ($platform) (https://github.com/Him188/ani)"
}

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