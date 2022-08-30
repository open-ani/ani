/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.animationgarden.app.app.data

import androidx.compose.runtime.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoNumber
import me.him188.animationgarden.api.impl.model.*
import me.him188.animationgarden.api.protocol.CommitsModule
import me.him188.animationgarden.app.app.StarredAnime
import me.him188.animationgarden.app.app.settings.LocalSyncSettings
import me.him188.animationgarden.app.app.settings.RemoteSyncSettings
import net.mamoe.yamlkt.Yaml
import kotlin.coroutines.CoroutineContext

interface AppDataSynchronizer {
    val appDataFlow: Flow<AppData>

    suspend fun getData(): AppData

    suspend fun commit(mutation: DataMutation)

    suspend fun saveNow()
}

abstract class SynchronizationException : Exception()

interface RemoteSynchronizer {
    @Stable
    val isSynchronized: State<Boolean>

    /**
     * sync and mark online
     */
    suspend fun syncOfflineHistory(/* commitHistory: */)

    @Throws(SynchronizationException::class)
    suspend fun commit(mutation: DataMutation, newData: AppData)

    fun markOffline()
}

class RemoteSynchronizerImpl(
    private val httpClient: HttpClient,
    private val remoteSettings: RemoteSyncSettings
) : RemoteSynchronizer {
    override val isSynchronized: MutableState<Boolean> = mutableStateOf(false)

    override suspend fun syncOfflineHistory() {
        // TODO: 2022/9/7 check!
        isSynchronized.value = true
    }

    @Serializable
    private data class CommitRequest(
        val mutation: @Polymorphic DataMutation,
        val newData: AppData,
    )

    override suspend fun commit(mutation: DataMutation, newData: AppData) {
        if (!isSynchronized.value) {
            syncOfflineHistory()
        }
        val req = CommitRequest(mutation, newData)
        val resp = httpClient.post {
            url {
                takeFrom(remoteSettings.apiUrl)
                appendPathSegments("commit", remoteSettings.token)
            }
            setBody(json.encodeToString(CommitRequest.serializer(), req))
        }
        println(resp.bodyAsText())
    }

    private val json = Json {
        serializersModule = CommitsModule
    }

    override fun markOffline() {
        isSynchronized.value = false
    }
}


class AppDataSynchronizerImpl(
    coroutineContext: CoroutineContext,
    private val remoteSynchronizer: RemoteSynchronizer?,
    private val backingStorage: MutableProperty<String>,
    private val localSyncSettingsFlow: Flow<LocalSyncSettings>,
    /**
     * `true`: user agrees to switch to offline mode.
     */
    private val promptSwitchToOffline: suspend (SynchronizationException) -> Boolean,
) : AppDataSynchronizer {
    private val scope =
        CoroutineScope(coroutineContext + SupervisorJob(coroutineContext[Job]) + CoroutineName("LocalAppdataSynchronizer"))

    private var memory: DataMutationContext? = null
    private val loadDataLock = Mutex()

    override val appDataFlow: Flow<AppData> = flow { emit(getData()) }

    override suspend fun getData(): DataMutationContext {
        memory?.let { return it }
        loadDataLock.withLock {
            memory?.let { return it }
            remoteSynchronizer?.syncOfflineHistory()
            return load(backingStorage.get()).also { memory = it }
        }
    }

    private val saverJob: Job = scope.launch {
        val localSyncSettings = withContext(Dispatchers.Main) {
            localSyncSettingsFlow.stateIn(this)
        }
        while (isActive) {
            memory?.let { doSave(it) }
            delay(localSyncSettings.value.checkInternal)
        }
    }

    @Serializable
    private class SerialData(
        @ProtoNumber(1) val starredAnime: List<StarredAnime> = listOf(),
    )

    private class DataMutationContextImpl(
        override val starredAnime: MutableListFlow<StarredAnime>
    ) : DataMutationContext

    private fun load(string: String): DataMutationContext {
        val decoded = Yaml.decodeFromString(SerialData.serializer(), string)
        return DataMutationContextImpl(mutableListFlowOf(decoded.starredAnime))
    }

    private fun dump(data: AppData): String {
        return Yaml.encodeToString(SerialData.serializer(), data.run {
            SerialData(starredAnime.value)
        })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val saveDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val mutationLock = Mutex()


    override suspend fun commit(mutation: DataMutation) {
        mutationLock.withLock {
            val data = getData()
            with(data) { mutation.invoke() }
            remoteSynchronizer?.let { remoteSynchronizer ->
                try {
                    remoteSynchronizer.commit(mutation, data)
                } catch (e: SynchronizationException) {
                    e.printStackTrace()
                    if (!promptSwitchToOffline.invoke(e)) {
                        // user doesn't want to switch to offline mode
                        with(data) { mutation.revoke() }
                    } else {
                        remoteSynchronizer.markOffline()
                    }
                }
            }
        }
    }

    override suspend fun saveNow() {
        memory?.let { doSave(it) }
    }

    private suspend fun doSave(data: AppData) {
        withContext(saveDispatcher) {
            memory?.let { backingStorage.set(dump(data)) }
        }
    }
}