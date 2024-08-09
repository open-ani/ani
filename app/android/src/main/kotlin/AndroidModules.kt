package me.him188.ani.android

import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import me.him188.ani.android.activity.MainActivity
import me.him188.ani.android.navigation.AndroidBrowserNavigator
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.source.media.resolver.AndroidWebVideoSourceResolver
import me.him188.ani.app.data.source.media.resolver.HttpStreamingVideoSourceResolver
import me.him188.ani.app.data.source.media.resolver.LocalFileVideoSourceResolver
import me.him188.ani.app.data.source.media.resolver.TorrentVideoSourceResolver
import me.him188.ani.app.data.source.media.resolver.VideoSourceResolver
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.AndroidPermissionManager
import me.him188.ani.app.platform.PermissionManager
import me.him188.ani.app.platform.notification.AndroidNotifManager
import me.him188.ani.app.platform.notification.NotifManager
import me.him188.ani.app.tools.DocumentsContractApi19
import me.him188.ani.app.tools.torrent.DefaultTorrentManager
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.tools.update.AndroidUpdateInstaller
import me.him188.ani.app.tools.update.UpdateInstaller
import me.him188.ani.app.videoplayer.ExoPlayerStateFactory
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File
import java.io.IOException

private const val BT_CACHE_DIR_CHANGED = "BT 存储位置不可用，已切换回默认存储位置。"

fun getAndroidModules(
    defaultTorrentCacheDir: File,
    coroutineScope: CoroutineScope,
) = module {
    single<PermissionManager> {
        AndroidPermissionManager()
    }
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
        val context = androidContext()
        val logger = logger<TorrentManager>()
        val defaultTorrentCachePath = defaultTorrentCacheDir.absolutePath
        val cacheDir = runBlocking {
            val settings = get<SettingsRepository>().mediaCacheSettings
            val dir = settings.flow.first().saveDir

            val resetToDefault = suspend {
                settings.update { copy(saveDir = defaultTorrentCachePath) }
                Toast.makeText(context, BT_CACHE_DIR_CHANGED, Toast.LENGTH_LONG).show()
                defaultTorrentCachePath
            }

            // 首次启动设置为应用内部私有目录
            if (dir == null) {
                settings.update { copy(saveDir = defaultTorrentCachePath) }
                return@runBlocking defaultTorrentCachePath
            }

            if (dir.startsWith(context.filesDir.absolutePath)) {
                // 在设置中保存的是私有目录，直接返回
                return@runBlocking dir
            }

            context.contentResolver.persistedUriPermissions.forEach { p ->
                val storage = DocumentsContractApi19.parseUriToStorage(context, p.uri)

                if (storage != null && dir.startsWith(storage)) {
                    return@runBlocking if (p.isReadPermission && p.isWritePermission) {
                        // 需要再次验证目录权限
                        try {
                            withContext(Dispatchers.IO) {
                                File(storage, ".nomedia").apply { createNewFile() }.writeText(" ")
                            }
                            dir
                        } catch (ex: IOException) {
                            // 实际上没有权限，释放 uri
                            logger.warn(ex) { "failed to write to .nomedia" }
                            context.contentResolver.releasePersistableUriPermission(
                                p.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                            )
                            resetToDefault()
                        }
                    } else {
                        // 在设置中保存的外部共享目录没有完整的读写权限，直接切换回默认的内部私有目录,
                        // 避免读写权限不足错误导致 App 崩溃
                        resetToDefault()
                    }
                }
            }
            // 既不是内部私有目录也不是外部共享目录，那一定是外部私有目录
            if (context.getExternalFilesDir(null) == null) {
                // 外部私有目录不可用，直接切换回默认的私有目录，避免读写权限不足错误导致 App 崩溃
                return@runBlocking resetToDefault()
            }

            // 外部私有目录可用
            dir
        }

        DefaultTorrentManager(
            coroutineScope.coroutineContext,
            saveDir = { Path(cacheDir).inSystem },
        )
    }
    single<PlayerStateFactory> { ExoPlayerStateFactory() }


    factory<VideoSourceResolver> {
        VideoSourceResolver.from(
            get<TorrentManager>().engines
                .map { TorrentVideoSourceResolver(it) }
                .plus(LocalFileVideoSourceResolver())
                .plus(HttpStreamingVideoSourceResolver())
                .plus(AndroidWebVideoSourceResolver()),
        )
    }
    single<UpdateInstaller> { AndroidUpdateInstaller() }
}
