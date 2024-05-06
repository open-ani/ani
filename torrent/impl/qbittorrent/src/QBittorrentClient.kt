package me.him188.ani.app.torrent.qbittorrent

import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import io.ktor.http.setCookie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import me.him188.ani.utils.ktor.ClientProxyConfig
import me.him188.ani.utils.ktor.createDefaultHttpClient
import me.him188.ani.utils.ktor.proxy
import me.him188.ani.utils.ktor.userAgent
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Serializable
data class QBittorrentClientConfig(
    val baseUrl: String = "http://127.0.0.1:8080",
    val username: String? = null,
    val password: String? = null,
    val proxy: ClientProxyConfig? = null,
    val userAgent: String? = null,
    val category: String = "Ani",
)

class QBittorrentClient(
    private val config: QBittorrentClientConfig,
) : AutoCloseable {
    private companion object {
        private val logger = logger<QBittorrentClient>()
    }

    private val clientLazy = lazy {
        createDefaultHttpClient {
            config.proxy?.let { proxy(it) }
            config.userAgent?.let { userAgent(it) }
            expectSuccess = true

            // 这 log 格式太蠢了
//            Logging {
//                level = LogLevel.INFO
//            }

            install(HttpRequestRetry) {
                maxRetries = 3
                // retry on rate limit error.
                retryIf { _, response -> response.status.value.let { it == 429 } }
                exponentialDelay()
            }

            val baseUrl = config.baseUrl.removeSuffix("/")
            defaultRequest {
                sid.value?.let { headers.append("SID", it) }
                url("$baseUrl/api/")
            }
        }
    }

    private suspend inline fun <R> autoLogin(block: () -> R): R {
        contract { callsInPlace(block, InvocationKind.AT_LEAST_ONCE) }

        return try {
            block()
        } catch (e: ServerResponseException) {
            if (e.response.status == HttpStatusCode.Forbidden || e.response.status == HttpStatusCode.Unauthorized) {
                logger.info { "Login required, attempting to login" }
                login()
                logger.info { "Login successful, retry block" }
                block()
            } else {
                throw e
            }
        }
    }

    private val client by clientLazy

    private val sid: MutableStateFlow<String?> = MutableStateFlow(null)

    suspend fun login() {
        if (config.username == null) {
            logger.info { "No username provided, skipping login" }
            return
        }
        val resp = client.post("v2/auth/login") {
            setBody("username=${config.username}&password=${config.password.orEmpty()}")
        }
        resp.setCookie().firstOrNull { it.name == "SID" }?.let {
            sid.value = it.value
        }
    }

    suspend fun getVersion(): String = autoLogin {
        val resp = client.get("v2/app/version").bodyAsText()
        logger.info { "qBittorrent version: $resp" }
        return resp
    }

    suspend fun getGlobalTransferInfo(): GlobalTransferInfo = autoLogin {
        return client.get("v2/transfer/info").body()
    }

    suspend fun getTorrentList(
        hashes: List<String>? = null,
    ): List<QBTorrent> = autoLogin {
        return client.get("v2/torrents/info") {
            parameter("category", config.category)
            if (!hashes.isNullOrEmpty()) {
                parameter("hashes", hashes.joinToString("|"))
            }
        }.body()
    }

//    suspend fun addTorrent(
//        urls: List<String>,
//        savePath: String,
//    ) {
//        client.post("torrents/add") {
//            parameter("savepath", savePath)
//            setBody(torrent)
//        }
//    }

    suspend fun getTorrentProperties(
        hash: String,
    ): QBTorrent = autoLogin {
        return client.get("v2/torrents/properties") {
            parameter("hash", hash)
        }.body()
    }

    suspend fun getTorrentFiles(
        hash: String,
        indexes: List<Int>? = null,
    ): List<QBFile> = autoLogin {
        return client.get("v2/torrents/files") {
            parameter("hash", hash)
            if (indexes != null) {
                parameter("indexes", indexes.joinToString("|"))
            }
        }.body()
    }

    suspend fun getPieceStates(
        hash: String,
    ): List<QBPieceState> = autoLogin {
        return client.get("v2/torrents/pieceStates") {
            parameter("hash", hash)
        }.body<List<Int>>().map { QBPieceState.entries[it] }
    }

    suspend fun pauseTorrents(
        hashes: List<String>,
    ) = autoLogin {
        client.submitForm(
            "v2/torrents/pause",
            formParameters = parameters {
                append("hashes", hashes.joinToString("|"))
            }
        )
    }

    suspend fun resumeTorrents(
        hashes: List<String>,
    ) = autoLogin {
        client.submitForm(
            "v2/torrents/resume",
            formParameters = parameters {
                append("hashes", hashes.joinToString("|"))
            }
        )
    }

    suspend fun deleteTorrents(
        hashes: List<String>,
        deleteFiles: Boolean,
    ) = autoLogin {
        client.submitForm(
            "v2/torrents/delete",
            formParameters = parameters {
                append("hashes", hashes.joinToString("|"))
                append("deleteFiles", deleteFiles.toString())
            }
        )
    }

    suspend fun addTorrentFromUri(
        uri: String,
        savePath: String, // C:/Users/qBit/Downloads
        paused: Boolean,
    ) = autoLogin {
        client.submitForm(
            "v2/torrents/add",
            formParameters = parameters {
                append("urls", uri) // supports http, https, magnet, "bc://bt/".
                append("savepath", savePath)
                append("category", config.category)
                append("root_folder", "true")
                append("paused", paused.toString())
            }
        )
    }

    suspend fun addTorrentFromData(
        data: ByteArray,
        filename: String,
        savePath: String, // C:/Users/qBit/Downloads
    ) = autoLogin {
        client.submitFormWithBinaryData(
            "v2/torrents/add",
            formData = formData {
                append("torrents", data, Headers.build {
                    append(
                        HttpHeaders.ContentDisposition,
                        "form-data; name=\"torrents\"; filename=\"$filename.torrent\""
                    )
                })
                append("savepath", savePath)
                append("category", config.category)
            },
        )
    }

    suspend fun setFilePriority(
        hash: String,
        index: Int,
        priority: QBFilePriority,
    ) = autoLogin {
        client.submitForm(
            "v2/torrents/filePrio",
            formParameters = parameters {
                append("hash", hash)
                append("id", index.toString())
                append("priority", priority.value.toString())
            }
        )
    }

    // not needed, actually automatically created by qBittorrent
//    suspend fun addNewCategory(
//        category: String,
//    ) {
//        client.post("v2/torrents/createCategory") {
//            parameter("category", category)
//        }
//    }

    suspend fun setFirstLastPiecePriority(
        hashes: List<String>
    ) = autoLogin {
        client.submitForm(
            "v2/torrents/toggleFirstLastPiecePrio",
            formParameters = parameters {
                append("hashes", hashes.joinToString("|"))
            }
        )
    }

    override fun close() {
        if (clientLazy.isInitialized()) {
            this.client.close()
        }
    }
}
