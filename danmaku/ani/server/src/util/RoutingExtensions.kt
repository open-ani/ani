package me.him188.ani.danmaku.server.util

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext

/**
 * Shortcut functions for `authenticate("auth-bearer") { <routingMethod>("path") { ... } }`.
 */

@KtorDsl
inline fun Route.getAuthenticated(
    path: String = "",
    optional: Boolean = false,
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-jwt", optional = optional) {
        get(path) { block() }
    }
}

@KtorDsl
inline fun Route.postAuthenticated(
    path: String = "",
    optional: Boolean = false,
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-jwt", optional = optional) {
        post(path) { block() }
    }
}

@KtorDsl
inline fun Route.putAuthenticated(
    path: String = "",
    optional: Boolean = false,
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-jwt", optional = optional) {
        put(path) { block() }
    }
}

@KtorDsl
inline fun Route.patchAuthenticated(
    path: String = "",
    optional: Boolean = false,
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-jwt", optional = optional) {
        patch(path) { block() }
    }
}

@KtorDsl
inline fun Route.deleteAuthenticated(
    path: String = "",
    optional: Boolean = false,
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-jwt", optional = optional) {
        delete(path) { block() }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.getUserIdOrRespond(): String? {
    return call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString().also {
        if (it == null) call.respond(HttpStatusCode.Unauthorized, "Invalid token")
    }
}

