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
import me.him188.ani.app.data.EpisodeRepository
import me.him188.ani.app.data.EpisodeRepositoryImpl
import me.him188.ani.app.data.PreferredAllianceRepository
import me.him188.ani.app.data.PreferredAllianceRepositoryImpl
import me.him188.ani.app.data.SubjectRepository
import me.him188.ani.app.data.SubjectRepositoryImpl
import me.him188.ani.app.data.TokenRepository
import me.him188.ani.app.data.TokenRepositoryImpl
import me.him188.ani.app.persistent.preferredAllianceStore
import me.him188.ani.app.persistent.tokenStore
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.session.SessionManagerImpl
import me.him188.ani.app.torrent.TorrentDownloaderManager
import me.him188.ani.app.torrent.TorrentDownloaderManagerImpl
import me.him188.ani.datasources.api.DownloadProvider
import me.him188.ani.datasources.api.SubjectProvider
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.BangumiSubjectProvider
import me.him188.ani.datasources.dmhy.DmhyClient
import me.him188.ani.datasources.dmhy.DmhyDownloadProvider
import org.koin.dsl.module

fun getCommonKoinModule(getContext: () -> Context, coroutineScope: CoroutineScope) = module {
    single<TokenRepository> { TokenRepositoryImpl(getContext().tokenStore) }
    single<PreferredAllianceRepository> { PreferredAllianceRepositoryImpl(getContext().preferredAllianceStore) }
    single<SessionManager> { SessionManagerImpl(coroutineScope) }
    single<DmhyClient> {
        DmhyClient.create {
            install(UserAgent) {
                agent = getAniUserAgent(currentAniBuildConfig.versionName)
            }
        }
    }
    single<BangumiClient> { createBangumiClient() }
    single<SubjectProvider> { BangumiSubjectProvider(get<BangumiClient>()) }
    single<DownloadProvider> { DmhyDownloadProvider() }
    single<SubjectRepository> { SubjectRepositoryImpl() }
    single<EpisodeRepository> { EpisodeRepositoryImpl() }
    single<TorrentDownloaderManager> { TorrentDownloaderManagerImpl(coroutineScope.coroutineContext) }
}

@Stable
interface AniBuildConfig {
    val versionName: String
    val bangumiOauthClientId: String
    val bangumiOauthClientSecret: String
    val isDebug: Boolean

    companion object
}

@Stable
expect val currentAniBuildConfig: AniBuildConfig

fun getAniUserAgent(
    version: String,
    platform: String = Platform.currentPlatform().name,
): String {
    return "him188/ani/$version ($platform) (https://github.com/Him188/ani)"
}

expect fun createBangumiClient(): BangumiClient