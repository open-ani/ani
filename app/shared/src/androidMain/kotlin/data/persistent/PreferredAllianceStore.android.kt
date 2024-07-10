package me.him188.ani.app.data.persistent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import me.him188.ani.app.platform.Context

actual val Context.preferredAllianceStore: DataStore<Preferences> by preferencesDataStore("preferredAlliances") 