package me.him188.ani.android

import me.him188.ani.android.navigation.AndroidAuthorizationNavigator
import me.him188.ani.android.navigation.AndroidBrowserNavigator
import me.him188.ani.android.navigation.AndroidSubjectNavigator
import me.him188.ani.app.navigation.AuthorizationNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.navigation.SubjectNavigator
import me.him188.ani.app.torrent.TorrentDownloader
import org.koin.dsl.module
import java.io.File

fun getAndroidModules(
    torrentCacheDir: File,
) = module {
    single<SubjectNavigator> { AndroidSubjectNavigator() }
    single<AuthorizationNavigator> { AndroidAuthorizationNavigator() }
    single<BrowserNavigator> { AndroidBrowserNavigator() }
    single<TorrentDownloader> { TorrentDownloader(torrentCacheDir) }
}