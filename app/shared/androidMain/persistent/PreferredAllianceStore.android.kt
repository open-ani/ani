package me.him188.ani.app.persistent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import me.him188.ani.app.platform.Context

actual val Context.preferredAllianceStore: DataStore<Preferences> by preferencesDataStore("preferredAlliances")
actual val Context.episodePositionStore: DataStore<Preferences> by preferencesDataStore("episodePosition")
actual val Context.appSettingsStore: DataStore<Preferences> by preferencesDataStore("appSettings")