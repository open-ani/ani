package me.him188.ani.app.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

interface EpisodeHistoryRepository : Repository {
    val flow: Flow<List<EpisodeHistory>>

    suspend fun clear()
    suspend fun remove(episodeId: Int)
    suspend fun saveOrUpdate(episodeId: Int, positionMillis: Long)
    suspend fun getPositionMillisByEpisodeId(episodeId: Int): Long?
}

@Serializable
data class EpisodeHistory(
    val episodeId: Int,
    val positionMillis: Long
)

@Serializable
data class EpisodeHistories(
    val histories: List<EpisodeHistory> = emptyList(),
) {
    companion object {
        val Empty = EpisodeHistories(emptyList())
    }
}

class EpisodeHistoryRepositoryImpl(
    private val dataStore: DataStore<EpisodeHistories>
) : EpisodeHistoryRepository {
    override val flow: Flow<List<EpisodeHistory>> = dataStore.data.map { it.histories }

    override suspend fun clear() {
        dataStore.updateData { EpisodeHistories.Empty }
    }

    override suspend fun remove(episodeId: Int) {
        dataStore.updateData { current ->
            current.copy(histories = current.histories.filter { it.episodeId != episodeId })
        }
    }

    override suspend fun saveOrUpdate(episodeId: Int, positionMillis: Long) {
        val episodeHistory = EpisodeHistory(
            episodeId = episodeId,
            positionMillis = positionMillis,
        )
        dataStore.updateData { current ->
            val history = current.histories.find { it.episodeId == episodeId }
            return@updateData if (history == null) {
                current.copy(histories = current.histories + episodeHistory)
            } else {
                current.copy(
                    histories = current.histories.map { save ->
                        if (save.episodeId == episodeHistory.episodeId) {
                            episodeHistory
                        } else {
                            save
                        }
                    },
                )
            }
        }
    }

    override suspend fun getPositionMillisByEpisodeId(episodeId: Int): Long? {
        return dataStore.data.map { current ->
            current.histories.find { it.episodeId == episodeId }?.positionMillis
        }.firstOrNull()
    }
}
