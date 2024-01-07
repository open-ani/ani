/*
 * Ani
 * Copyright (C) 2022-2024 Him188
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

package me.him188.ani.app.app.settings

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.ktor.client.engine.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ProxySettings(
    @Stable
    val mode: ProxyMode = ProxyMode.DISABLED,
    @Stable
    val http: HttpProxy = HttpProxy("http://localhost:7890"),
    @Stable
    val socks: SocksProxy = SocksProxy("localhost", 7890)
)


enum class ProxyMode {
    DISABLED,
    HTTP,
    SOCKS
}


@Immutable
@Serializable
sealed class Proxy

@Serializable
@SerialName("http")
@Immutable
data class HttpProxy(
    @Stable
    val url: String
) : Proxy()

@Serializable
@SerialName("socks")
@Immutable
data class SocksProxy(
    @Stable
    val host: String,
    @Stable
    val port: Int,
) : Proxy()

@Stable
fun ProxySettings.toKtorProxy(): ProxyConfig? = kotlin.runCatching {
    when (mode) {
        ProxyMode.HTTP -> {
            ProxyBuilder.http(http.url)
        }

        ProxyMode.SOCKS -> {
            ProxyBuilder.socks(socks.host, socks.port)
        }

        ProxyMode.DISABLED -> null
    }
}.getOrNull()
