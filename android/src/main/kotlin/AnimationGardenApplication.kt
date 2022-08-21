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
import androidx.compose.runtime.Stable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.app.app.ApplicationState
import me.him188.animationgarden.app.app.LocalAppSettingsManagerImpl
import me.him188.animationgarden.app.app.toKtorProxy

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
                    ApplicationState(
                        initialClient = AnimationGardenClient.Factory.create {
                            proxy =
                                appSettingsManager.value.value.proxy.toKtorProxy() // android thinks this is doing network operation
                        },
                        workingDir = workingDir
                    )
                }
            }
    }

    override fun onCreate() {
        super.onCreate()
        instance = Instance(this)
    }
}
