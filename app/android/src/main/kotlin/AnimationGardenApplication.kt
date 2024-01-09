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
import android.view.View
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.him188.ani.R
import me.him188.ani.app.activity.BaseComponentActivity
import me.him188.ani.app.activity.showSnackbarAsync
import me.him188.ani.app.app.LocalAppSettingsManagerImpl
import me.him188.ani.app.i18n.ResourceBundle
import me.him188.ani.app.i18n.loadResourceBundle
import me.him188.ani.app.platform.getCommonKoinModule
import me.him188.ani.app.ux.showDialog
import org.koin.core.context.startKoin
import java.io.File
import kotlin.coroutines.resume

class AniApplication : Application() {
    private val tag = AniApplication::class.simpleName

    companion object {
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
        @Stable
        val workingDir: File = context.filesDir

        @Stable
        val appSettingsManager by lazy {
            LocalAppSettingsManagerImpl(workingDir.resolve("data/settings.dat"))
                .apply { load() }
        }

        // Use LocalI18n in compose
        @Stable
        lateinit var resourceBundle: ResourceBundle

        init {
            @OptIn(DelicateCoroutinesApi::class)
            startKoin {
                modules(getCommonKoinModule({ context }, GlobalScope))
                modules(AndroidModules)
            }
        }

        // do not observe dependency change
        @Stable
        val app by lazy {
            runBlocking(Dispatchers.Default) {
                val settings = appSettingsManager.value.value // initialize in Main thread
                withContext(Dispatchers.IO) {
                    val tag by lazy(LazyThreadSafetyMode.PUBLICATION) { context.getString(R.string.app_package) }
                    val appScope =
                        CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
                            Log.e(tag, "Unhandled exception in coroutine", throwable)
                        })

                    val currentBundle = loadResourceBundle(context)
                    resourceBundle = currentBundle
                }
            }
        }
    }

    private suspend fun promptSwitchToOffline(
        optional: Boolean,
        currentBundle: ResourceBundle,
        exception: Exception,
    ) = if (optional) {
        showDialog { cont ->
            setPositiveButton(
                String.format(
                    currentBundle.getString("sync.failed.content"),
                    exception.render()
                )
            ) { _, _ ->
                cont.resume(true)
                showSnackbarShort(currentBundle.getString("sync.failed.switched.to.offline"))
            }

            setNegativeButton(currentBundle.getString("sync.failed.revoke")) { _, _ ->
                cont.resume(false)
                showSnackbarShort(currentBundle.getString("sync.failed.revoked"))
            }
        }
    } else {
        showSnackbarLong(
            String.format(
                currentBundle.getString("sync.failed.switched.to.offline.due.to"),
                exception.render()
            )
        )
        true
    }

    private fun Exception.render() = message ?: toString()

    private fun showSnackbarLong(message: String) {
        (currentActivity as? BaseComponentActivity)?.showSnackbarAsync(
            message,
            duration = SnackbarDuration.Short
        )
    }

    private fun showSnackbarShort(message: String) {
        (currentActivity as? BaseComponentActivity)?.showSnackbarAsync(
            message,
            duration = SnackbarDuration.Short
        )
    }

    private fun Activity.getRootView() =
        this.findViewById<View>(android.R.id.content)?.rootView

    override fun onCreate() {
        super.onCreate()
        instance = Instance(this)
    }
}
