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
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.media.LocalFileVideoSourceResolver
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.media.MediaCacheManagerImpl
import me.him188.ani.app.data.media.MediaSourceManager
import me.him188.ani.app.data.media.MediaSourceManagerImpl
import me.him188.ani.app.data.media.TorrentMediaCacheEngine
import me.him188.ani.app.data.media.TorrentVideoSourceResolver
import me.him188.ani.app.data.media.VideoSourceResolver
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
import org.koin.dsl.module

fun getCommonKoinModule(getContext: () -> Context, coroutineScope: CoroutineScope) = module {
    // Repositories
    single<TokenRepository> { TokenRepositoryImpl(getContext().tokenStore) }
    single<EpisodePreferencesRepository> { EpisodePreferencesRepositoryImpl(getContext().preferredAllianceStore) }
    single<SessionManager> { SessionManagerImpl() }
    single<BangumiClient> { createBangumiClient() }
    single<SubjectProvider> { BangumiSubjectProvider(get<BangumiClient>()) }
    single<SubjectRepository> { SubjectRepositoryImpl() }
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
    single<VideoSourceResolver> {
        VideoSourceResolver.from(
            TorrentVideoSourceResolver(get()),
            LocalFileVideoSourceResolver(),
        )
    }
    single<MediaCacheManager> {
        MediaCacheManagerImpl(
            listOf(
                DirectoryMediaCacheStorage(
                    "local-default",
                    getContext().files.cacheDir.resolve("media").toPath(),
                    TorrentMediaCacheEngine(
                        "local-default",
                        getTorrentDownloader = { get<TorrentManager>().downloader.await() },
                    ),
                    coroutineScope.coroutineContext
                )
            )
        )
    }
    single<MediaSourceManager> {
        MediaSourceManagerImpl(
            additionalSources = {
                get<MediaCacheManager>().storages.map { it.cacheMediaSource }
            }
        )
    }
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