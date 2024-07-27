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
import android.util.Log
import androidx.compose.runtime.Stable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.him188.ani.app.i18n.ResourceBundle
import me.him188.ani.app.i18n.loadResourceBundle
import me.him188.ani.app.platform.AndroidLoggingConfigurator
import me.him188.ani.app.platform.AndroidLoggingConfigurator.getLogsDir
import me.him188.ani.app.platform.createAppRootCoroutineScope
import me.him188.ani.app.platform.getCommonKoinModule
import me.him188.ani.app.platform.startCommonKoinModule
import me.him188.ani.app.tools.torrent.TorrentManager
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
        registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacks {
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
            },
        )
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

        // since 3.5, 删除 libtorrent4j 缓存, 大概保留到 3.8 就可以删除个代码了
        torrentCaches.resolve("libtorrent4j").let {
            if (it.exists()) {
                it.deleteRecursively()
                Log.w("AniApplication", "Deleted libtorrent4j cache")
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
