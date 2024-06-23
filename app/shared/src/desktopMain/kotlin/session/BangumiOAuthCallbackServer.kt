package me.him188.ani.app.session

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.random.Random

class BangumiOAuthCallbackServer(
    private val onCodeReceived: suspend BangumiOAuthCallbackServer.(code: String) -> Unit // runs on Ktor thread.
) : AutoCloseable {
    private val server by lazy {
        embeddedServer(CIO, port = Random.nextInt(40000, 50000)) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
            routing {
                get("/bangumi-oauth-callback") {
                    val code = call.request.queryParameters["code"] ?: run {
                        call.respondText("code is null", status = HttpStatusCode.BadRequest)
                        return@get
                    }
                    call.respondText(
                        "<h3>您现在可以关闭本页面并返回 ani</h3>",
                        contentType = ContentType.Text.Html,
                        status = HttpStatusCode.OK,
                    )
                    onCodeReceived(code)
                }
            }
        }
    }

    fun start() {
        server.start()
    }

    fun getCallbackUrl(): String {
        return server.environment.connectors.first().run {
            "http://127.0.0.1:$port/bangumi-oauth-callback"
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun close() {
        GlobalScope.launch(Dispatchers.IO) {
            server.stop(0, 0)
        }
    }
}