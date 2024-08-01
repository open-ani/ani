package me.him188.ani.app.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.source.media.instance.MediaSourceSave
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.mikan.MikanCNMediaSource
import me.him188.ani.utils.platform.Uuid

interface MediaSourceInstanceRepository : Repository {
    val flow: Flow<List<MediaSourceSave>>

    suspend fun clear()
    suspend fun remove(instanceId: String)
    suspend fun add(mediaSourceSave: MediaSourceSave)

    suspend fun updateSave(instanceId: String, config: MediaSourceSave.() -> MediaSourceSave)
    suspend fun reorder(newOrder: List<String>)
}

suspend inline fun MediaSourceInstanceRepository.updateConfig(instanceId: String, config: MediaSourceConfig) {
    updateSave(instanceId) {
        copy(config = config)
    }
}

@Serializable
data class MediaSourceSaves(
    val instances: List<MediaSourceSave> = emptyList(),
) {
    companion object {
        val Empty = MediaSourceSaves(emptyList())
        val Default: MediaSourceSaves by lazy {
            fun createSave(it: String, isEnabled: Boolean) = MediaSourceSave(
                instanceId = Uuid.randomString(),
                mediaSourceId = it,
                isEnabled = isEnabled,
                config = MediaSourceConfig.Default,
            )

            val enabledWebSources: List<String> =
                listOf("nyafun", "mxdongman", "ntdm", "gugufan")
            val enabledBtSources: List<String> =
                listOf(MikanCNMediaSource.ID, "dmhy")
            val disabledBtSources: List<String> = listOf()

            MediaSourceSaves(
                buildList {
                    enabledWebSources.forEach { add(createSave(it, isEnabled = true)) }
                    enabledBtSources.forEach { add(createSave(it, isEnabled = true)) }
                    disabledBtSources.forEach { add(createSave(it, isEnabled = false)) }
                },
            )
        }
    }
}

class MediaSourceInstanceRepositoryImpl(
    private val dataStore: DataStore<MediaSourceSaves>
) : MediaSourceInstanceRepository {
    override val flow: Flow<List<MediaSourceSave>> = dataStore.data.map { it.instances }
    override suspend fun clear() {
        dataStore.updateData { MediaSourceSaves.Empty }
    }

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

    override suspend fun updateSave(instanceId: String, config: MediaSourceSave.() -> MediaSourceSave) {
        dataStore.updateData { current ->
            current.copy(
                instances = current.instances.map { save ->
                    if (save.instanceId == instanceId) {
                        save.run(config)
                    } else {
                        save
                    }
                },
            )
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
