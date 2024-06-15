package me.him188.ani.android

import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.android.activity.MainActivity
import me.him188.ani.android.navigation.AndroidBrowserNavigator
import me.him188.ani.app.data.media.resolver.AndroidWebVideoSourceResolver
import me.him188.ani.app.data.media.resolver.HttpStreamingVideoSourceResolver
import me.him188.ani.app.data.media.resolver.LocalFileVideoSourceResolver
import me.him188.ani.app.data.media.resolver.TorrentVideoSourceResolver
import me.him188.ani.app.data.media.resolver.VideoSourceResolver
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.notification.AndroidNotifManager
import me.him188.ani.app.platform.notification.NotifManager
import me.him188.ani.app.tools.torrent.DefaultTorrentManager
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.tools.update.AndroidUpdateInstaller
import me.him188.ani.app.tools.update.UpdateInstaller
import me.him188.ani.app.videoplayer.ExoPlayerStateFactory
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File

fun getAndroidModules(
    torrentCacheDir: File,
    coroutineScope: CoroutineScope,
) = module {
    single<NotifManager> {
        AndroidNotifManager(
            NotificationManagerCompat.from(androidContext()),
            getContext = { androidContext() },
            activityIntent = {
                Intent(androidContext(), MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
//                androidContext().packageManager.getLaunchIntentForPackage(androidContext().packageName)
//                    ?: Intent(Intent.ACTION_MAIN).apply {
//                        setPackage(androidContext().packageName)
//                    }
            },
            coroutineScope.coroutineContext,
        ).apply { createChannels() }
    }
    single<BrowserNavigator> { AndroidBrowserNavigator() }
    single<TorrentManager> {
        DefaultTorrentManager(
            coroutineScope.coroutineContext,
            saveDir = { torrentCacheDir },
        )
    }
    single<PlayerStateFactory> { ExoPlayerStateFactory() }


    factory<VideoSourceResolver> {
        VideoSourceResolver.from(
            get<TorrentManager>().engines
                .map { TorrentVideoSourceResolver(it) }
                .plus(LocalFileVideoSourceResolver())
                .plus(HttpStreamingVideoSourceResolver())
                .plus(AndroidWebVideoSourceResolver())
        )
    }
    single<UpdateInstaller> { AndroidUpdateInstaller() }
}
