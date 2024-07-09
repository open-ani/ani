package me.him188.ani.app.platform.persistent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import me.him188.ani.app.platform.Context

expect val Context.preferredAllianceStore: DataStore<Preferences>
