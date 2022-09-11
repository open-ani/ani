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

package me.him188.animationgarden.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.server.websocket.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.him188.animationgarden.api.protocol.*
import mu.KotlinLogging
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import java.io.File

class CommitManager(
    baseRef: CommitRef,
) {
    private val _currentBaseRef = atomic(baseRef)

    inner class Locked

    val headRef
        get() = _currentBaseRef.value

    fun setHead(ref: CommitRef) {
        _currentBaseRef.value = ref
    }

    @PublishedApi
    internal val lock = Mutex()

    private val _eventFlow: MutableSharedFlow<CommitEvent> = MutableSharedFlow()
    val eventFlow: SharedFlow<CommitEvent> = _eventFlow.asSharedFlow()

    context(Locked) suspend fun submitCommit(request: PushCommitRequest): CommitRef? {
        if (request.base != headRef) return null

        val newRef = CommitRef.generate()
        val event = CommitEvent(request.base, newRef, request.commit, request.committer)
        _eventFlow.emit(event)
        _currentBaseRef.value = newRef
        return newRef
    }

    @PublishedApi
    internal val locked = Locked()

    suspend inline fun <R> transaction(block: context(Locked, CommitManager) () -> R): R {
        return lock.withLock {
            block(locked, this)
        }
    }
}

fun Application.configureCommitsModule(
    folder: File,
) {
    val commitManagers = mutableMapOf<String, CommitManager>()
    fun getCommitManager(token: String): CommitManager {
        return commitManagers.getOrPut(token) {
            val commitFile = folder.resolve("$token.commit")
            if (!commitFile.exists()) {
                commitFile.writeText(CommitRef.generate().toString())
            }
            CommitManager(CommitRef(commitFile.readText()))
        }
    }

    logger.info { "Dumping initial refs." }
    for (commitFile in folder.listFiles().orEmpty().filter { it.extension == "commit" }) {
        if (commitFile.isFile) {
            logger.info { "${commitFile.nameWithoutExtension.takeLast(8)}: ${commitFile.readText()}" }
        }
    }

    routing {
        route("/data/head/{token}") {
            get {
                val token = call.parameters.getOrFail("token")
                val commitManager = getCommitManager(token)

                val file = folder.resolve("$token.dat")
                if (file.exists()) {
                    val headRef = commitManager.headRef
                    logger.info(marker) { withClient { "Request all data. Ref: $headRef" } }
                    call.respond(
                        HttpStatusCode.OK,
                        GetHeadResponse(
                            data = protobuf.decodeFromByteArray(EAppData.serializer(), file.readBytes()),
                            ref = headRef
                        )
                    )
                } else {
                    logger.info(marker) { withClient { "Request all data. No data found." } }
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            put {
                val token = call.parameters.getOrFail("token")
                val commitManager = getCommitManager(token)

                val request = call.receive<SetHeadRequest>()


                commitManager.setHead(request.ref)

                val file = folder.resolve("$token.dat")
                val newFile = file.exists()
                file.writeBytes(protobuf.encodeToByteArray(EAppData.serializer(), request.data))

                if (newFile) {
                    logger.info(marker) { withClient { "Force push created. New ref: ${request.ref}" } }
                    call.respond(HttpStatusCode.Created)
                } else {
                    logger.info(marker) { withClient { "Force push accepted. New ref: ${request.ref}" } }
                    call.respond(HttpStatusCode.Accepted)
                }
            }
        }
        webSocket("/data/sync/{token}") {
            val token = call.parameters.getOrFail("token")
            val clientId = call.parameters.getOrFail("clientId")
            val commitManager = getCommitManager(token)

            commitManager.eventFlow.filter {
                it.committer.uuid != clientId
            }.collect { event ->
                logger.info(marker) { withClient { "Broadcast CommitEvent: ${event.toLogString()}" } }
                outgoing.send(Frame.Binary(true, protobuf.encodeToByteArray(CommitEvent.serializer(), event)))
            }
        }
        route("/data/commit/{token}") {
            get {
                val token = call.parameters.getOrFail("token")
                val commitManager = getCommitManager(token)
                val ref = commitManager.headRef
                logger.info(marker) { withClient { "Fetch head. Returned $ref" } }
                call.respond(HttpStatusCode.OK, SyncRefResponse(ref))
            }

            post {
                val token = call.parameters.getOrFail("token")
                val file = folder.resolve("$token.dat")
                val req = call.receive<PushCommitRequest>()

                val commitManager = getCommitManager(token)
                val result = commitManager.transaction {
                    submitCommit(req)
                }

                if (result != null) {
                    logger.info(marker) { withClient { "Commit '${req.commit.toLogString()}' based on ${req.base}: accepted. New ref: ${commitManager.headRef}" } }
                    call.respond(
                        HttpStatusCode.Accepted,
                        PushCommitResponse(PushCommitResult.Success(result))
                    )
                    file.writeBytes(protobuf.encodeToByteArray(EAppData.serializer(), req.newData))
                } else {
                    logger.info(marker) { withClient { "Commit '${req.commit.toLogString()}' based on ${req.base}: conflict with current base ${commitManager.headRef}" } }
                    call.respond(
                        HttpStatusCode.Conflict,
                        PushCommitResponse(PushCommitResult.OutOfDate(commitManager.headRef))
                    )
                }
            }
        }
    }
}

private object CommitModule

private val logger = KotlinLogging.logger { }
private val marker: Marker = MarkerFactory.getMarker(CommitModule::class.qualifiedName)

internal inline fun PipelineContext<*, ApplicationCall>.withClient(message: () -> String): String {
    val token = call.parameters.getOrFail("token").takeLast(8)
    val clientId = call.parameters["clientId"]?.takeLast(12)
    return "$token/$clientId: ${message()}"
}

internal inline fun DefaultWebSocketServerSession.withClient(message: () -> String): String {
    val token = call.parameters.getOrFail("token").takeLast(8)
    val clientId = call.parameters["clientId"]?.takeLast(12)
    return "$token/$clientId: ${message()}"
}