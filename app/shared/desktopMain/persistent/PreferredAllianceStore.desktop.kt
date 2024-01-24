package me.him188.ani.app.persistent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import me.him188.ani.app.platform.Context

actual val Context.preferredAllianceStore: DataStore<Preferences>
    get() = TODO("Not yet implemented")
actual val Context.episodePositionStore: DataStore<Preferences>
    get() = TODO("Not yet implemented")
actual val Context.appSettingsStore: DataStore<Preferences>
    get() = TODO("Not yet implemented")