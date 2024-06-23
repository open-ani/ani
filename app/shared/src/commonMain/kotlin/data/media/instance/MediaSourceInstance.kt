package me.him188.ani.app.data.media.instance

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import java.io.Closeable
import java.util.UUID

/**
 * [MediaSource], 以及它的配置, 统称为 [MediaSourceInstance].
 */
@Stable
class MediaSourceInstance(
    val instanceId: String, // uuid, to be persisted
    val mediaSourceId: String,
    val isEnabled: Boolean,
    val config: MediaSourceConfig,
    val source: MediaSource,
) : Closeable {
    override fun close() {
        source.close()
    }
}

// 持久化在文件里
@Serializable
data class MediaSourceSave(
    val instanceId: String,
    val mediaSourceId: String,
    val isEnabled: Boolean,
    val config: MediaSourceConfig,
)

fun createTestMediaSourceInstance(
    source: MediaSource,
    instanceId: String = UUID.randomUUID().toString(),
    mediaSourceId: String = source.mediaSourceId,
    isEnabled: Boolean = true,
    config: MediaSourceConfig = MediaSourceConfig.Default,
): MediaSourceInstance {
    return MediaSourceInstance(
        instanceId = instanceId,
        mediaSourceId = mediaSourceId,
        isEnabled = isEnabled,
        config = config,
        source = source,
    )
}
