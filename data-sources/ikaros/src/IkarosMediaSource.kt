package me.him188.ani.datasources.ikaros

import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.HttpMediaSource
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.useHttpClient
import me.him188.ani.utils.logging.logger
import java.nio.charset.StandardCharsets
import java.util.Base64

class IkarosMediaSource(config: MediaSourceConfig) : HttpMediaSource() {
    companion object {
        const val ID = "ikaros"
        val logger = logger<IkarosMediaSource>()
        val BASE_URL = getEnv("ANI_DS_IKAROS_BASE_URL")
        val USERNAME = getEnv("ANI_DS_IKAROS_USERNAME")
        val PASSWORD = getEnv("ANI_DS_IKAROS_PASSWORD")
        private fun getEnv(envName: String?): String {
            if (envName.isNullOrEmpty()) {
                return ""
            }
            val env = System.getenv(envName)
            if (env == null || env.isEmpty()) {
                return ""
            }
            return env
        }
    }

    internal val client = IkarosClient(BASE_URL, useHttpClient(config) {
        defaultRequest {
            header(
                HttpHeaders.Authorization,
                "Basic " + Base64.getEncoder().encodeToString("$USERNAME:$PASSWORD".toByteArray(StandardCharsets.UTF_8))
            )
        }
    })

    class Factory : MediaSourceFactory {
        override val mediaSourceId: String get() = ID

        override fun create(config: MediaSourceConfig): MediaSource = IkarosMediaSource(config)
    }

    override val kind: MediaSourceKind get() = MediaSourceKind.WEB

    override val mediaSourceId: String get() = ID

    override suspend fun checkConnection(): ConnectionStatus {
        return if ((HttpStatusCode.OK == client.checkConnection())
        ) ConnectionStatus.SUCCESS else ConnectionStatus.FAILED
    }

    override suspend fun fetch(query: MediaFetchRequest): SizedSource<MediaMatch> {
        val subjectId = checkNotNull(query.subjectId)
        val episodeSort = checkNotNull(query.episodeSort.number?.toInt())
        val ikarosSubjectDetails = checkNotNull(client.postSubjectSyncBgmTv(subjectId))
        return client.subjectDetails2SizedSource(ikarosSubjectDetails, episodeSort)
    }
}
