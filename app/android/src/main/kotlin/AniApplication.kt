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

package me.him188.ani.android

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Stable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.media.TorrentMediaCacheEngine
import me.him188.ani.app.i18n.ResourceBundle
import me.him188.ani.app.i18n.loadResourceBundle
import me.him188.ani.app.platform.AndroidLoggingConfigurator
import me.him188.ani.app.platform.AndroidLoggingConfigurator.getLogsDir
import me.him188.ani.app.platform.createAppRootCoroutineScope
import me.him188.ani.app.platform.getCommonKoinModule
import me.him188.ani.app.platform.startCommonKoinModule
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.torrent.libtorrent4j.Libtorrent4jTorrentDownloader
import me.him188.ani.app.torrent.qbittorrent.QBittorrentTorrentDownloader
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AniApplication : Application() {
    companion object {
        init {
            if (BuildConfig.DEBUG) {
                System.setProperty("kotlinx.coroutines.debug", "on")
                System.setProperty("kotlinx.coroutines.stacktrace.recovery", "true")
            }
//            @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
//            val v = kotlinx.coroutines.RECOVER_STACK_TRACES
//            println(v)
        }

        lateinit var instance: Instance
    }

    private var currentActivity: Activity? = null

    init {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
                if (currentActivity == activity) {
                    currentActivity = null
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }

    inner class Instance(context: Context) {
        // Use LocalI18n in compose
        @Stable
        lateinit var resourceBundle: ResourceBundle

        // do not observe dependency change
        @Stable
        val app by lazy {
            runBlocking(Dispatchers.Default) {
                withContext(Dispatchers.IO) {
                    val currentBundle = loadResourceBundle(context)
                    resourceBundle = currentBundle
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        AndroidLoggingConfigurator.configure(applicationContext.getLogsDir().absolutePath)

        instance = Instance(this)

        val scope = createAppRootCoroutineScope()

        // 将下载数据放在数据目录防止被系统清除
        val torrentCaches = applicationContext.filesDir.resolve("torrent-caches").apply { mkdir() }

        /**
         * 迁移过去后 metadata 保存的 EXTRA_TORRENT_DATA 仍然是旧的,
         * 但是 [TorrentMediaCacheEngine.deleteUnusedCaches] 可以处理这种情况.
         *
         * [Libtorrent4jTorrentDownloader] 可以支持新的目录.
         * [QBittorrentTorrentDownloader] 也支持, 不过目前 PC 上数据仍然在缓存目录.
         */
        applicationContext.cacheDir.resolve("torrent-caches").let {
            if (it.exists()) {
                it.copyRecursively(torrentCaches, true)
                it.deleteRecursively()
            }
        }

        startKoin {
            androidContext(this@AniApplication)
            modules(getCommonKoinModule({ this@AniApplication }, scope))
            modules(getAndroidModules(torrentCaches, scope))
        }.startCommonKoinModule(scope)

        getKoin().get<TorrentManager>() // start sharing, connect to DHT now
    }
}
