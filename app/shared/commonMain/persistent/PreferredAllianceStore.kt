package me.him188.ani.app.persistent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import me.him188.ani.app.platform.Context

expect val Context.preferredAllianceStore: DataStore<Preferences>
expect val Context.episodePositionStore: DataStore<Preferences>
expect val Context.appSettingsStore: DataStore<Preferences>