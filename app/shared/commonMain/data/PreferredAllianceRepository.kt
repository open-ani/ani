package me.him188.ani.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent

interface PreferredAllianceRepository : KoinComponent {
    fun preferredAlliance(subjectId: Int): Flow<String?>
    suspend fun setPreferredAlliance(subjectId: Int, allianceName: String)
}

internal class PreferredAllianceRepositoryImpl(
    private val store: DataStore<Preferences>,
) : PreferredAllianceRepository {
    private val logger = logger(this::class)

    override fun preferredAlliance(subjectId: Int): Flow<String?> {
        return store.data.map { it[stringPreferencesKey(subjectId.toString())] }
    }

    override suspend fun setPreferredAlliance(subjectId: Int, allianceName: String) {
        logger.info { "Saved user preferred alliance for subject $subjectId: $allianceName" }
        store.edit {
            it[stringPreferencesKey(subjectId.toString())] = allianceName
        }
    }
}