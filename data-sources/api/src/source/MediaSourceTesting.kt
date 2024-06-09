package me.him188.ani.datasources.api.source

import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.paging.emptySizedSource
import java.util.UUID
import kotlin.random.Random


open class TestHttpMediaSource(
    override val mediaSourceId: String = UUID.randomUUID().toString(),
    override val kind: MediaSourceKind = MediaSourceKind.BitTorrent,
    private val randomConnectivity: Boolean = false,
    private val fetch: suspend (MediaFetchRequest) -> SizedSource<MediaMatch> = { emptySizedSource() }
) : HttpMediaSource() {
    override suspend fun checkConnection(): ConnectionStatus {
        if (randomConnectivity) {
            return Random.nextBoolean().let {
                if (it) ConnectionStatus.SUCCESS else ConnectionStatus.FAILED
            }
        }
        return ConnectionStatus.SUCCESS
    }

    override suspend fun fetch(query: MediaFetchRequest): SizedSource<MediaMatch> = this.fetch.invoke(query)
}
