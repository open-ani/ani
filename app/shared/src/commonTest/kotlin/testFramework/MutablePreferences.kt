package me.him188.ani.app.testFramework

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.Preferences.Key

fun preferencesOf(
    vararg pairs: Pair<Key<*>, Any>,
): Preferences = mutablePreferencesOf(*pairs, startFrozen = true)

fun mutablePreferencesOf(
    vararg pairs: Pair<Key<*>, Any>,
    startFrozen: Boolean = false,
): MutablePreferences {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    return MutablePreferences(pairs.toMap(LinkedHashMap(pairs.size)), startFrozen)
}
