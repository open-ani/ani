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

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import io.ktor.client.plugins.logging.*
import kotlinx.coroutines.*
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.impl.createHttpClient
import me.him188.animationgarden.api.protocol.CommitRef
import me.him188.animationgarden.app.app.ApplicationState
import me.him188.animationgarden.app.app.LocalAppSettingsManagerImpl
import me.him188.animationgarden.app.app.data.AppDataSynchronizerImpl
import me.him188.animationgarden.app.app.data.map
import me.him188.animationgarden.app.app.settings.createFileDelegatedMutableProperty
import me.him188.animationgarden.app.app.settings.createLocalStorage
import me.him188.animationgarden.app.app.settings.createRemoteSynchronizer
import me.him188.animationgarden.app.app.settings.toKtorProxy
import mu.KotlinLogging
import org.slf4j.MarkerFactory

class AnimationGardenApplication : Application() {
    companion object {
        lateinit var instance: Instance
    }

    class Instance(context: Context) {
        @Stable
        val workingDir = context.filesDir

        @Stable
        val appSettingsManager = LocalAppSettingsManagerImpl(workingDir.resolve("data/settings.yml"))
            .apply { load() }

        @Stable
        val app =
            // do not observe dependency change
            runBlocking {
                withContext(Dispatchers.IO) {
                    val tag by lazy(LazyThreadSafetyMode.PUBLICATION) { context.getString(R.string.app_package) }
                    val appScope =
                        CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
                            Log.e(tag, "Unhandled exception in coroutine", throwable)
                        })

                    val settings = appSettingsManager.value.value

                    ApplicationState(
                        initialClient = AnimationGardenClient.Factory.create {
                            proxy =
                                settings.proxy.toKtorProxy() // android thinks this is doing network operation
                        },
                        appDataSynchronizer = { syncScope ->
                            AppDataSynchronizerImpl(
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
                                        localRef = createFileDelegatedMutableProperty(workingDir.resolve("data/commit")).map(
                                            get = { CommitRef(it) },
                                            set = { it.toString() },
                                        ),
                                        promptConflict = {
                                            TODO("prompt")
                                        },
                                        applyMutation = applyMutation,
                                        parentCoroutineContext = syncScope.coroutineContext
                                    )
                                },
                                backingStorage = settings.sync.createLocalStorage(
                                    workingDir.resolve("data/app.yml").apply { parentFile.mkdir() }),
                                localSyncSettingsFlow = snapshotFlow {
                                    settings.sync.localSync
                                },
                                promptSwitchToOffline = { exception, optional ->
                                    TODO("prompt")
                                }
                            )
                        },
                        applicationScope = appScope,
                    )
                }
            }
    }

    override fun onCreate() {
        super.onCreate()
        instance = Instance(this)
    }
}
