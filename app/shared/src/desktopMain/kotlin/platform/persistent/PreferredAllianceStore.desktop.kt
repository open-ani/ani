package me.him188.ani.app.platform.persistent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import me.him188.ani.app.platform.Context
import me.him188.ani.app.platform.DesktopContext

actual val Context.preferredAllianceStore: DataStore<Preferences> get() = (this as DesktopContext).preferredAllianceStore