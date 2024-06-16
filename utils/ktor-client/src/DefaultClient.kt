package me.him188.ani.utils.ktor

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.him188.ani.utils.ktor.HttpLogger.logHttp
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.slf4j.Logger
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun createDefaultHttpClient(
    clientConfig: HttpClientConfig<*>.() -> Unit = {},
) = HttpClient {
    install(HttpRequestRetry) {
        maxRetries = 1
        delayMillis { 1000 }
    }
    install(HttpCookies)
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
    }
    BrowserUserAgent()
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
    followRedirects = true
    clientConfig()
}

fun HttpClient.registerLogging(
    logger: Logger = logger("ktor"),
) {
    plugin(HttpSend).intercept { request ->
        val t1 = System.nanoTime()
        val result = kotlin.runCatching { execute(request) }
        val duration = ((System.nanoTime() - t1) / 1e6).toDuration(DurationUnit.MILLISECONDS)

        logger.logHttp(
            method = request.method,
            url = request.url.toString(),
            isAuthorized = request.headers.contains(HttpHeaders.Authorization),
            responseStatus = result.getOrNull()?.response?.status,
            duration = duration
        )
        result.getOrThrow()
    }
}

object HttpLogger {
    fun Logger.logHttp(
        method: HttpMethod,
        url: String,
        isAuthorized: Boolean,
        responseStatus: HttpStatusCode?, // null means failed
        duration: Duration,
    ) {
        when {
            // 刻意没记录 exception, 因为外面应该会处理
            responseStatus == null ->
                error { buildHttpRequestLog(method, url, isAuthorized, null, duration) }

            responseStatus.isSuccess() ->
                info { buildHttpRequestLog(method, url, isAuthorized, responseStatus, duration) }

            else -> warn { buildHttpRequestLog(method, url, isAuthorized, responseStatus, duration) }
        }
    }

    fun buildHttpRequestLog(
        method: HttpMethod,
        url: String,
        isAuthorized: Boolean,
        responseStatus: HttpStatusCode?, // null means failed
        duration: Duration,
    ): String {
        val methodStr = method.value.padStart(5, ' ')
        return buildString {
            append(methodStr)
            append(" ")
            append(url)
            append(" ")
            if (isAuthorized) {
                append("[Authorized]")
            }

            append(": ")

            if (responseStatus != null) {
                append(responseStatus.toString()) // 404 Not Found
            } else {
                append("FAILED")
            }

            append(" in ")
            append(duration.toString())
        }
    }
}

fun HttpLogger.buildHttpRequestLog(
    request: HttpRequestBuilder,
    call: HttpClientCall?,
    duration: Duration,
): String = buildHttpRequestLog(
    method = request.method,
    url = request.url.toString(),
    isAuthorized = request.headers.contains(HttpHeaders.Authorization),
    responseStatus = call?.response?.status,
    duration = duration
)
