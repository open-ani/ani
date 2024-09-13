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

package me.him188.ani.app.data.persistent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import me.him188.ani.app.platform.Context
import me.him188.ani.app.platform.DesktopContext
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.io.toKtPath

actual val Context.dataStoresImpl: PlatformDataStoreManager
    get() = PlatformDataStoreManagerDesktop(this as DesktopContext)

internal class PlatformDataStoreManagerDesktop(
    private val context: DesktopContext,
) : PlatformDataStoreManager() {
    override val tokenStore: DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.dataStoreDir.resolve("tokens.preferences_pb")
    }
    override val preferencesStore: DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.dataStoreDir.resolve("settings.preferences_pb")
    }
    override val preferredAllianceStore: DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.dataStoreDir.resolve("preferredAllianceStore.preferences_pb")
    }

    override fun resolveDataStoreFile(name: String): SystemPath = context.dataStoreDir.resolve(name).toKtPath().inSystem
}
