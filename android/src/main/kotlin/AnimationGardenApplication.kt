/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.android

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import io.ktor.client.plugins.logging.*
import kotlinx.coroutines.*
import me.him188.animationgarden.android.activity.BaseComponentActivity
import me.him188.animationgarden.android.activity.showSnackbarAsync
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.impl.createHttpClient
import me.him188.animationgarden.api.protocol.CommitRef
import me.him188.animationgarden.app.app.AppSettings
import me.him188.animationgarden.app.app.ApplicationState
import me.him188.animationgarden.app.app.LocalAppSettingsManagerImpl
import me.him188.animationgarden.app.app.data.AppDataSynchronizerImpl
import me.him188.animationgarden.app.app.data.ConflictAction
import me.him188.animationgarden.app.app.data.map
import me.him188.animationgarden.app.app.settings.createFileDelegatedMutableProperty
import me.him188.animationgarden.app.app.settings.createLocalStorage
import me.him188.animationgarden.app.app.settings.createRemoteSynchronizer
import me.him188.animationgarden.app.app.settings.toKtorProxy
import me.him188.animationgarden.app.i18n.ResourceBundle
import me.him188.animationgarden.app.i18n.loadResourceBundle
import mu.KotlinLogging
import org.slf4j.MarkerFactory
import java.io.File
import kotlin.coroutines.resume

class AnimationGardenApplication : Application() {
    private val tag = AnimationGardenApplication::class.simpleName

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
        val appSettingsManager = LocalAppSettingsManagerImpl(workingDir.resolve("data/settings.yml"))
            .apply { load() }

        // Use LocalI18n in compose
        @Stable
        lateinit var resourceBundle: ResourceBundle

        // do not observe dependency change
        @Stable
        val app by lazy {
            runBlocking {
                withContext(Dispatchers.IO) {
                    val tag by lazy(LazyThreadSafetyMode.PUBLICATION) { context.getString(R.string.app_package) }
                    val appScope =
                        CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
                            Log.e(tag, "Unhandled exception in coroutine", throwable)
                        })

                    val settings = appSettingsManager.value.value
                    val currentBundle = loadResourceBundle(context)
                    resourceBundle = currentBundle

                    ApplicationState(
                        initialClient = AnimationGardenClient.Factory.create {
                            proxy =
                                settings.proxy.toKtorProxy() // android thinks this is doing network operation
                        },
                        appDataSynchronizer = { syncScope ->
                            createAppDataSynchronizer(
                                syncScope,
                                settings,
                                this@AnimationGardenApplication,
                                context,
                                currentBundle
                            )
                        },
                        applicationScope = appScope,
                    )
                }
            }
        }
    }

    private fun createAppDataSynchronizer(
        syncScope: CoroutineScope,
        settings: AppSettings,
        animationGardenApplication: AnimationGardenApplication,
        context: Context,
        currentBundle: ResourceBundle
    ) = AppDataSynchronizerImpl(
        syncScope.coroutineContext,
        remoteSynchronizerFactory = { applyMutation ->
            settings.sync.createRemoteSynchronizer(
                httpClient = createHttpClient(clientConfig = {
                    install(Logging) {
                        logger = object : Logger {
                            private val delegate = KotlinLogging.logger {}
                            private val marker = MarkerFactory.getMarker("HTTP")
                            override fun log(message: String) {
                                delegate.info(marker, message)
                            }
                        }
                        level = LogLevel.BODY
                    }
                }),
                localRef = createFileDelegatedMutableProperty(instance.workingDir.resolve("data/commit")).map(
                    get = { CommitRef(it) },
                    set = { it.toString() },
                ),
                promptConflict = {
                    animationGardenApplication.promptConflict(currentBundle)
                },
                applyMutation = applyMutation,
                parentCoroutineContext = syncScope.coroutineContext
            )
        },
        backingStorage = settings.sync.createLocalStorage(
            instance.workingDir.resolve("data/app.yml").apply { parentFile.mkdir() }),
        localSyncSettingsFlow = snapshotFlow {
            settings.sync.localSync
        },
        promptSwitchToOffline = { exception, optional ->
            Log.e(tag, "promptSwitchToOffline exception", exception)
            promptSwitchToOffline(optional, context, currentBundle, exception)
        },
        promptDataCorrupted = { exception ->
            (currentActivity as? BaseComponentActivity)?.showSnackbarAsync(
                String.format(
                    currentBundle.getString("sync.data.corrupted"),
                    exception.render()
                ),
                duration = SnackbarDuration.Indefinite,
                withDismissAction = true,
            )
        }
    )

    private suspend fun promptSwitchToOffline(
        optional: Boolean,
        context: Context,
        currentBundle: ResourceBundle,
        exception: Exception
    ) = if (optional) {
        suspendCancellableCoroutine { cont ->
            val dialog = AlertDialog.Builder(context)
                .setPositiveButton(
                    String.format(
                        currentBundle.getString("sync.failed.content"),
                        exception.render()
                    )
                ) { _, _ ->
                    cont.resume(true)
                    showSnackbarShort(currentBundle.getString("sync.failed.switched.to.offline"))
                }
                .setNegativeButton(currentBundle.getString("sync.failed.revoke")) { _, _ ->
                    cont.resume(false)
                    showSnackbarShort(currentBundle.getString("sync.failed.revoked"))
                }
                .create()
            dialog.show()
            cont.invokeOnCancellation {
                dialog.hide()
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

    private suspend fun promptConflict(
        currentBundle: ResourceBundle
    ): ConflictAction {
        val currentActivity = currentActivity ?: return ConflictAction.StayOffline
        return suspendCancellableCoroutine { cont ->
            val dialog = AlertDialog.Builder(currentActivity)
                .setTitle(currentBundle.getString("sync.conflict.dialog.title"))
                .setMessage(currentBundle.getString("sync.conflict.dialog.content"))
                .setCancelable(false)
                .setPositiveButton(currentBundle.getString("sync.conflict.dialog.useServer")) { _, _ ->
                    cont.resume(ConflictAction.AcceptServer)
                }
                .setNegativeButton(currentBundle.getString("sync.conflict.dialog.useLocal")) { _, _ ->
                    cont.resume(ConflictAction.AcceptClient)
                }
                .setNeutralButton(currentBundle.getString("sync.conflict.dialog.offline")) { _, _ ->
                    cont.resume(ConflictAction.StayOffline)
                }
                .create()
            dialog.show()
            cont.invokeOnCancellation {
                dialog.hide()
            }
        }
    }

    private fun Activity.getRootView() =
        this.findViewById<View>(android.R.id.content)?.rootView

    override fun onCreate() {
        super.onCreate()
        instance = Instance(this)
    }
}

//private class ConflictDialogFragment : DialogFragment() {
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val currentBundle = AnimationGardenApplication.instance.resourceBundle
//
//        return MaterialAlertDialogBuilder(requireContext())
//            .setTitle(currentBundle.getString("sync.conflict.dialog.title"))
//            .setMessage(currentBundle.getString("sync.conflict.dialog.content"))
//            .setCancelable(true)
//            .setItems(
//                arrayOf(
//                    currentBundle.getString("sync.conflict.dialog.useServer"),
//                    currentBundle.getString("sync.conflict.dialog.useLocal"),
//                    currentBundle.getString("sync.conflict.dialog.offline"),
//                )
//            ) { _, which ->
//                when (which) {
//                    0 -> cont.resume(ConflictAction.AcceptServer)
//                    1 -> cont.resume(ConflictAction.AcceptClient)
//                    2 -> cont.resume(ConflictAction.StayOffline)
//                }
//            }
//            .setOnCancelListener {
//                cont.resume(ConflictAction.StayOffline)
//            }
//            .create()
//    }
//}