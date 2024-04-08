package me.him188.ani.utils.ktor

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.http
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ClientProxyConfig(
    /**
     * "http://sample-proxy-server:3128/"
     * or
     * "socks5://sample-proxy-server:3128"
     */
    val url: String,

    /**
     * ProxyAuthorization header
     *
     * "Basic username:password"
     */
    val authorization: String? = null
)

object ClientProxyConfigValidator {
    fun parseProxy(url: String): ProxyConfig = if (url.startsWith("socks")) {
        Url(url).run {
            ProxyBuilder.socks(host, port)
        }
    } else {
        ProxyBuilder.http(url)
    }

    fun isValidProxy(url: String): Boolean = try {
        parseProxy(url)
        true
    } catch (e: Exception) {
        false
    }
}

fun HttpClientConfig<*>.userAgent(
    value: String?
) {
    value ?: return
    install(UserAgent) {
        agent = value
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun HttpClientConfig<*>.proxy(
    config: ClientProxyConfig?,
) {
    config ?: return
    engine {
        proxy = ClientProxyConfigValidator.parseProxy(config.url)
    }

    defaultRequest {
        val credentials = Base64.encode("jetbrains:foobar".toByteArray())
        header(HttpHeaders.ProxyAuthorization, "Basic $credentials")
    }
}
