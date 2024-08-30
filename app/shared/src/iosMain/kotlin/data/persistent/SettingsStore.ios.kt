package me.him188.ani.app.data.persistent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import me.him188.ani.app.platform.Context
import me.him188.ani.utils.io.SystemPath

/**
 * Must not be stored
 */
actual val Context.dataStoresImpl: PlatformDataStoreManager
    get() = IosPlatformDataStoreManager

object IosPlatformDataStoreManager : PlatformDataStoreManager() {
    override val tokenStore: DataStore<Preferences>
        get() = TODO("Not yet implemented")
    override val preferencesStore: DataStore<Preferences>
        get() = TODO("Not yet implemented")
    override val preferredAllianceStore: DataStore<Preferences>
        get() = TODO("Not yet implemented")

    override fun resolveDataStoreFile(name: String): SystemPath {
        TODO("Not yet implemented")
    }
}
