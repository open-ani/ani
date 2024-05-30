package me.him188.ani.datasources.ikaros

import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.utils.logging.logger
import kotlinx.coroutines.flow.Flow

class IkarosMediaSource(config: MediaSourceConfig) : MediaSource {
    companion object {
        const val ID = "ikaros"
        val logger = logger<IkarosMediaSource>()
        val BASE_URL = GetEnv("ANI_DS_IKAROS_BASE_URL")
        val USERNAME = GetEnv("ANI_DS_IKAROS_USERNAME")
        val PASSWORD = GetEnv("ANI_DS_IKAROS_PASSWORD")
        private fun GetEnv(envName: String?): String {
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

    internal val client = IkarosClient(BASE_URL, USERNAME, PASSWORD)

    class Factory : MediaSourceFactory {
        override val mediaSourceId: String get() = ID

        override fun create(config: MediaSourceConfig): MediaSource = IkarosMediaSource(config)
    }

    override val kind: MediaSourceKind get() = MediaSourceKind.WEB

    override val mediaSourceId: String get() = ID

    override suspend fun checkConnection(): ConnectionStatus {
        return if ((200 == client.checkConnection())
        ) ConnectionStatus.SUCCESS else ConnectionStatus.FAILED
    }

    override suspend fun fetch(query: MediaFetchRequest): SizedSource<MediaMatch> {
        val subjectId = checkNotNull(query.subjectId)
        val episodeSort = checkNotNull(query.episodeSort.number?.toInt())
        val ikarosSubjectDetails = checkNotNull(client.postSubjectSyncBgmTv(subjectId))
        return client.subjectDetails2SizedSource(ikarosSubjectDetails, episodeSort)
    }
}

class IkarosSizeSource(
    override val results: Flow<MediaMatch>,
    override val finished: Flow<Boolean>,
    override val totalSize: Flow<Int?>
) : SizedSource<MediaMatch> {
}