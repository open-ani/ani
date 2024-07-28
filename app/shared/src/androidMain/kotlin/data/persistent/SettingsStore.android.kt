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
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import me.him188.ani.app.platform.Context
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.io.toKtPath

actual val Context.dataStoresImpl: PlatformDataStoreManager
    get() = PlatformDataStoreManagerAndroid(this)

internal class PlatformDataStoreManagerAndroid(
    private val context: Context,
) : PlatformDataStoreManager() {
    override fun resolveDataStoreFile(name: String): SystemPath {
        return context.applicationContext.dataStoreFile(name).toKtPath().inSystem
    }

    private val Context.tokenStoreImpl by preferencesDataStore("tokens")
    override val tokenStore: DataStore<Preferences> get() = context.tokenStoreImpl

    private val Context.preferencesStoreImpl by preferencesDataStore("preferences")
    override val preferencesStore: DataStore<Preferences> get() = context.preferencesStoreImpl

    private val Context.preferredAlliancesStoreImpl by preferencesDataStore("preferredAlliances")
    override val preferredAllianceStore: DataStore<Preferences> get() = context.preferredAlliancesStoreImpl
}
