package me.him188.ani.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import me.him188.ani.app.app.AppSettings
import org.koin.core.component.KoinComponent

interface AppSettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun updateSettings(edit: AppSettings.() -> AppSettings)
}

internal class AppSettingsRepositoryImpl(
    private val store: DataStore<Preferences>
) : AppSettingsRepository, KoinComponent {
    override val settings: Flow<AppSettings> = store.data
        .map { preferences -> preferences[stringPreferencesKey("appSettings")] }
        .map { json -> json?.let { Json.decodeFromString(it) } ?: AppSettings() }

    override suspend fun updateSettings(edit: AppSettings.() -> AppSettings) {
        store.edit {
            val exists = it[stringPreferencesKey("appSettings")]
                ?.let { json -> Json.decodeFromString(json) }
                ?: AppSettings()
            exists.edit()
        }
    }
}