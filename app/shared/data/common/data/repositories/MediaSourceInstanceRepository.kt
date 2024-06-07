package me.him188.ani.app.data.repositories

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.core.instance.MediaSourceSave

interface MediaSourceInstanceRepository : Repository {
    val flow: Flow<List<MediaSourceSave>>

    suspend fun remove(instanceId: String)
    suspend fun add(mediaSourceSave: MediaSourceSave)

    suspend fun updateConfig(instanceId: String, config: MediaSourceConfig)
    suspend fun reorder(newOrder: List<String>)
}

@Serializable
data class MediaSourceSaves(
    val instances: List<MediaSourceSave> = emptyList(),
) {
    companion object {
        val Default = MediaSourceSaves()
    }
}

class MediaSourceInstanceRepositoryImpl(
    private val dataStore: DataStore<MediaSourceSaves>
) : MediaSourceInstanceRepository {
    override val flow: Flow<List<MediaSourceSave>> = dataStore.data.map { it.instances }

    override suspend fun remove(instanceId: String) {
        dataStore.updateData { current ->
            current.copy(instances = current.instances.filter { it.instanceId != instanceId })
        }
    }

    override suspend fun add(mediaSourceSave: MediaSourceSave) {
        dataStore.updateData { current ->
            current.copy(instances = current.instances + mediaSourceSave)
        }
    }

    override suspend fun updateConfig(instanceId: String, config: MediaSourceConfig) {
        dataStore.updateData { current ->
            current.copy(instances = current.instances.map { save ->
                if (save.instanceId == instanceId) {
                    save.copy(config = config)
                } else {
                    save
                }
            })
        }
    }

    override suspend fun reorder(newOrder: List<String>) {
        dataStore.updateData { current ->
            val instances = current.instances.toMutableList()
            val newInstances = newOrder.mapNotNull { instanceId ->
                current.instances.find { it.instanceId == instanceId }
            }
            current.copy(instances = newInstances + instances.filter { it.instanceId !in newOrder })
        }
    }
}
