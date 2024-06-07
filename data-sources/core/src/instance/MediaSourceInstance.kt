package me.him188.ani.datasources.core.instance

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import java.io.Closeable

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
