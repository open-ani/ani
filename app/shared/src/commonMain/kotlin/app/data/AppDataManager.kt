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
import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoNumber
import me.him188.animationgarden.api.impl.model.*
import me.him188.animationgarden.api.logging.info
import me.him188.animationgarden.api.logging.logger
import me.him188.animationgarden.api.logging.trace
import me.him188.animationgarden.api.model.Commit
import me.him188.animationgarden.api.model.toLogString
import me.him188.animationgarden.api.protocol.*
import me.him188.animationgarden.app.app.StarredAnime
import me.him188.animationgarden.app.app.settings.LocalSyncSettings
import me.him188.animationgarden.app.app.settings.RemoteSyncSettings
import java.net.ConnectException
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

interface AppDataSynchronizer {
    val appDataFlow: Flow<AppData>

    suspend fun getData(): AppData

    suspend fun commit(mutation: DataMutation)

    suspend fun saveNow()
}

abstract class SynchronizationException : Exception()

class FailedRequestException(override val message: String?, override val cause: Throwable? = null) :
    SynchronizationException()

interface RemoteSynchronizer {
    @Stable
    val isSynchronized: StateFlow<Boolean>

    /**
     * sync and mark online
     * @return new data
     */
    @Throws(SynchronizationException::class)
    suspend fun syncOfflineHistory(
        localData: MutableProperty<AppData>
    )

    @Throws(SynchronizationException::class)
    suspend fun pushCommit(
        mutation: DataMutation,
        newData: AppData,
        localData: MutableProperty<AppData>
    )

    fun markOffline()
}

sealed class ConflictAction {
    object AcceptServer : ConflictAction()
    object AcceptClient : ConflictAction()
    object StayOffline : ConflictAction()
}

class RemoteSynchronizerImpl(
    private val httpClient: HttpClient,
    private val remoteSettings: RemoteSyncSettings,
    private val localRef: MutableProperty<CommitRef>,
    private val promptConflict: suspend () -> ConflictAction,
    private val applyMutation: suspend (DataMutation) -> Unit,
    parentCoroutineContext: CoroutineContext,
) : RemoteSynchronizer {
    private companion object {
        private val logger = logger()
    }

    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))

    override val isSynchronized: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val lock = Mutex()
    private val clientId = UUID.randomUUID().toString()

    override suspend fun syncOfflineHistory(
        localData: MutableProperty<AppData>
    ) {
        logger.info { "Attempting to back on sync..." }
        lock.withLock {
            while (true) {
                val httpResp = httpClient.get {
                    url {
                        url(remoteSettings.apiUrl)
                        appendPathSegments("data", "commit", remoteSettings.token)
                        parameter("clientId", clientId)
                    }
                    contentType(ContentType.Application.Json)
                    setBody(SyncRefRequest())
                }
                httpResp.checkSuccess(::FailedRequestException)

                val resp = httpResp.body<SyncRefResponse>()

                val localRef = this.localRef.get()
                if (localRef != resp.ref) {
                    logger.info { "different ref" }

                    // check conflict
                    if (!handleDataConflict(resp.ref, localRef, localData)) continue
                }
                isSynchronized.value = true

                if (logger.isInfoEnabled) {
                    val newRef = this.localRef.get()
                    logger.info { "Client is now synced with server. localRef = $newRef" }
                }
                break
            }
        }
    }

    private suspend fun handleDataConflict(
        remoteRef: CommitRef,
        localRef: CommitRef,
        localData: MutableProperty<AppData>
    ): Boolean {
        when {
            remoteRef.isParentOf(localRef) -> {
                logger.info { "local updated while remote didn't change" }
                if (forcePushAllData(localData, remoteRef)) {
                    logger.info { "successfully force pushed" }
                } else {
                    logger.info { "server has changed just now, retrying" }
                    return false
                }
            }
            localRef.isParentOf(remoteRef) -> {
                logger.info { "remote updated while local didn't change" }
                if (useServerData(localData, remoteRef)) {
                    logger.info { "successfully downloaded newest data" }
                } else {
                    logger.info { "server has changed just now, retrying" }
                    return false
                }
            }
            else -> {
                // total conflict
                resolveDataConflict(localData)
            }
        }
        return true
    }

    private suspend fun resolveDataConflict(
        localData: MutableProperty<AppData>,
    ) {
        logger.info { "Data conflict detected" }
        when (promptConflict()) {
            ConflictAction.AcceptClient -> {
                logger.info { "Accepting client's data" }
                forcePushAllData(localData, null)
            }
            ConflictAction.AcceptServer -> {
                logger.info { "Accepting server's data" }
                useServerData(localData, null)
            }
            ConflictAction.StayOffline -> {
                logger.info { "Dropping sync" }
                isSynchronized.value = false
            }
        }
    }

    private suspend fun useServerData(
        localData: MutableProperty<AppData>,
        requireBasedOn: CommitRef?,
    ): Boolean {
        val httpResp = httpClient.get {
            url {
                url(remoteSettings.apiUrl)
                appendPathSegments("data", "head", remoteSettings.token)
                parameter("clientId", clientId)
            }
            contentType(ContentType.Application.Json)
            setBody(GetHeadRequest())
        }
        httpResp.checkSuccess(::FailedRequestException)

        val resp = httpResp.body<GetHeadResponse>()
        if (requireBasedOn != null && resp.ref != requireBasedOn) {
            return false
        }
        when (httpResp.status) {
            HttpStatusCode.NoContent -> {
                logger.info { "Server holds empty data, force pushing..." }
                forcePushAllData(localData, resp.ref)
                logger.info { "Pushed local data to server." }
                return true
            }
            else -> {
                // 200 OK
                logger.info { "ref=${resp.ref}" }
                localRef.set(resp.ref)
                logger.info { "Overriding client data with ${resp.data}" }
                localData.set(resp.data.toAppData())
                return true
            }
        }
    }

    private suspend fun forcePushAllData(localData: MutableProperty<AppData>, requireBasedOn: CommitRef?): Boolean {
        val data = localData.get()
        logger.info { "Pushing local data: $data" }
        val localRef = localRef.get()
        val httpResp = httpClient.put {
            url {
                url(remoteSettings.apiUrl)
                appendPathSegments("data", "head", remoteSettings.token)
                parameter("clientId", clientId)
                parameter("requireBasedOn", requireBasedOn)
            }
            contentType(ContentType.Application.Json)
            setBody(SetHeadRequest(data = data.toEAppData(), ref = localRef))
        }
        if (httpResp.status == HttpStatusCode.Conflict) return false
        httpResp.checkSuccess(::FailedRequestException)
        return true
    }

    private inline fun HttpResponse.checkSuccess(createException: (message: String) -> Throwable) {
        if (!status.isSuccess()) {
            throw createException("Http request failed: ${this.status}")
        }
    }


    init {
        scope.launch(CoroutineName("connectWebSocket")) {
            while (isActive) {
                try {
                    if (isSynchronized.value) {
                        connectWebSocket()
                    }
                } catch (e: ConnectException) {
                    logger.error("Connection refused")
                } catch (e: ConnectTimeoutException) {
                    logger.error("Timed out connecting to server")
                } catch (e: NoTransformationFoundException) {
                    logger.error(e.message)
                } catch (e: Exception) {
                    logger.error("Exception in data sync connection", e)
                }
                delay(5.seconds)
            }
        }
    }

    private suspend fun connectWebSocket() {
        logger.info { "Starting data sync connection to ${remoteSettings.apiUrl}" }

        val session = httpClient.webSocketSession {
            url {
                takeFrom(Url(remoteSettings.apiUrl))
                protocol = URLProtocol.WS
                appendPathSegments("data", "sync", remoteSettings.token)
                parameter("clientId", clientId)
                logger.info { "URL: ${buildString()}" }
            }
        }

        logger.info { "connection established" }
        session.incoming.consumeEach { frame ->
            try {
                handleSyncEvent(frame)
            } catch (e: Exception) {
                promptConflict()
                logger.error("Exception in data sync connection", e)
            }
        }
        logger.info { "WS connection closed normally" }

        session.coroutineContext.job.join()
    }

    private suspend fun handleSyncEvent(frame: Frame) {
        when (frame) {
            is Frame.Ping, is Frame.Pong -> {}
            is Frame.Binary -> {
                val event = protobuf.decodeFromByteArray(CommitEvent.serializer(), frame.data)
                if (event.committer.uuid == clientId) return

                // check ref
                val localRef = localRef.get()
                if (event.baseRef == localRef) {
                    this.localRef.set(event.newRef)
                    logger.trace { "Received event '${event.toLogString()}': accepted. New ref: ${event.newRef}" }
                } else {
                    logger.trace { "Received event '${event.toLogString()}': conflict." }
                    TODO("handle conflict")
//                    when(promptConflict()) {
//                        ConflictAction.AcceptClient -> {
//                            forcePushAllData()
//                        }
//                        ConflictAction.AcceptServer -> {
//                            useServerData()
//                        }
//                        ConflictAction.StayOffline -> {
//
//                        }
//                    }
                }

                val mutation = event.commit.toMutation()
                logger.trace { "Corresponding mutation: $mutation" }
                applyMutation(mutation)
            }
            else -> {
                logger.trace { "Received unsupported frame: $frame" }
            }
        }
    }

    override suspend fun pushCommit(
        mutation: DataMutation,
        newData: AppData,
        localData: MutableProperty<AppData>
    ) {
        if (!isSynchronized.value) {
            logger.trace { "Not pushing commit because offline" }
            return // offline mode
        }

        lock.withLock {
            // TODO: 2022/9/7 combined may not be working

            when (mutation) {
                is CombinedDataMutation -> {
                    pushCommit(mutation.first, newData, localData)
                    pushCommit(mutation.then, newData, localData)
                }
                is SingleDataMutation -> {
                    mutation.toCommit()?.let {
                        pushCommitImpl(it, newData, localData)
                    }
                }
            }
        }
    }

    private suspend fun pushCommitImpl(
        commit: Commit,
        newData: AppData,
        localData: MutableProperty<AppData>,
    ) {
        val ref = localRef.get()
        logger.info { "Pushing commit: ${commit::class.simpleName}. Local ref: $ref" }
        while (true) {
            val req = PushCommitRequest(ref, commit, newData.toEAppData(), committer = Committer(uuid = clientId))
            val httpResp = httpClient.post {
                url {
                    takeFrom(remoteSettings.apiUrl)
                    appendPathSegments("data", "commit", remoteSettings.token)
                    parameter("clientId", clientId)
                }
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            val resp = httpResp.body<PushCommitResponse>()
            when (val result = resp.result) {
                is PushCommitResult.Success -> {
                    val newRef = result.newHeadRef
                    logger.info { "Commit ${commit::class.simpleName} success. New ref: $newRef" }
                    localRef.set(newRef)
                }
                is PushCommitResult.OutOfDate -> {
                    logger.info { "Commit ${commit::class.simpleName} conflict" }
                    if (!handleDataConflict(result.newHeadRef, localRef.get(), localData)) continue
                }
            }
            break
        }
    }

    override fun markOffline() {
        isSynchronized.value = false
    }
}

typealias PromptSwitchToOffline = suspend (Exception, optional: Boolean) -> Boolean

class AppDataSynchronizerImpl(
    coroutineContext: CoroutineContext,
    remoteSynchronizerFactory: (applyMutation: suspend (DataMutation) -> Unit) -> RemoteSynchronizer?,
    private val backingStorage: MutableProperty<String>,
    private val localSyncSettingsFlow: Flow<LocalSyncSettings>, // should run with Main dispatcher
    /**
     * `true`: user agrees to switch to offline mode.
     */
    private val promptSwitchToOffline: PromptSwitchToOffline,
    private val promptDataCorrupted: suspend (Exception) -> Unit,
) : AppDataSynchronizer {
    private companion object {
        private val logger = logger()
    }

    private val scope =
        CoroutineScope(coroutineContext + SupervisorJob(coroutineContext[Job]) + CoroutineName("LocalAppdataSynchronizer"))

    private var memory: DataMutationContext? = null
    private val loadDataLock = Mutex()

    override val appDataFlow: Flow<AppData> = flow { emit(getData()) }

    private val localDataProperty: MutableProperty<AppData> = backingStorage.map(
        get = { load(it) },
        set = { dump(it) }
    )

    private val remoteSynchronizer = remoteSynchronizerFactory { mutation ->
        this.applyMutation(mutation)
    }

    override suspend fun getData(): DataMutationContext {
        memory?.let { return it }
        loadDataLock.withLock {
            memory?.let { return it }

            val remoteSynchronizer = remoteSynchronizer
            if (remoteSynchronizer == null) {
                logger.info { "Remote sync is not enabled." }
            } else {
                withContext(Dispatchers.IO) {
                    try {
                        remoteSynchronizer.syncOfflineHistory(localDataProperty)
                    } catch (e: Exception) {
                        promptSwitchToOffline.invoke(e, false)
                        remoteSynchronizer.markOffline()
                    }
                }
            }
            memory?.let { return it }
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

    private suspend fun load(string: String): DataMutationContext {
        val decoded = try {
            protobuf.decodeFromHexString(SerialData.serializer(), string)
        } catch (e: Exception) {
            kotlin.runCatching {
                jsonForMigration.decodeFromString(SerialData.serializer(), string)
            }.getOrElse {
                promptDataCorrupted(e)
                SerialData()
            }
        }
        return createDataMutationContext(decoded)
    }

    private fun createDataMutationContext(decoded: SerialData) =
        DataMutationContextImpl(mutableListFlowOf(decoded.starredAnime))

    private fun dump(data: AppData): String {
        return protobuf.encodeToHexString(SerialData.serializer(), data.run {
            SerialData(starredAnime.value)
        })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val saveDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val mutationLock = Mutex()


    override suspend fun commit(mutation: DataMutation) {
        logger.trace { "Commit: $mutation" }
        mutationLock.withLock {
            val data = applyMutation(mutation)
            remoteSynchronizer?.let { remoteSynchronizer ->
                try {
                    remoteSynchronizer.pushCommit(mutation, data, localDataProperty)
                } catch (e: SynchronizationException) {
                    e.printStackTrace()
                    if (!promptSwitchToOffline.invoke(e, true)) {
                        // user doesn't want to switch to offline mode
                        with(data) { mutation.revoke() }
                    } else {
                        remoteSynchronizer.markOffline()
                    }
                }
            }
        }
    }

    private suspend fun applyMutation(mutation: DataMutation): DataMutationContext {
        val data = getData()
        withContext(Dispatchers.Main.immediate) {
            with(data) { mutation.invoke() }
        }
        return data
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