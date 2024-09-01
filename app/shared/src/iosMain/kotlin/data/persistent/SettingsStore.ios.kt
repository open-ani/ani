package me.him188.ani.app.data.persistent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import kotlinx.io.files.Path
import me.him188.ani.app.platform.Context
import me.him188.ani.app.tools.caching.MemoryDataStore
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.io.resolve

/**
 * Must not be stored
 */
actual val Context.dataStoresImpl: PlatformDataStoreManager
    get() = IosPlatformDataStoreManager

object IosPlatformDataStoreManager : PlatformDataStoreManager() {
    // TODO: IOS IosPlatformDataStoreManager
    override val tokenStore: DataStore<Preferences> by lazy {
        MemoryDataStore(mutablePreferencesOf())
    }
    override val preferencesStore: DataStore<Preferences> by lazy {
        MemoryDataStore(mutablePreferencesOf())
    }
    override val preferredAllianceStore: DataStore<Preferences> by lazy {
        MemoryDataStore(mutablePreferencesOf())
    }

    override fun resolveDataStoreFile(name: String): SystemPath = Path(".").inSystem.resolve(name)
}
