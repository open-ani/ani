package me.him188.ani.app.data.models.preference

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes

@Serializable
data class AnitorrentConfig(
    /**
     * 设置为 [FileSize.Unspecified] 表示无限
     */
    val downloadRateLimit: FileSize = FileSize.Unspecified,
    /**
     * 设置为 [FileSize.Unspecified] 表示无限, [FileSize.Zero] 表示不允许上传
     */
    val uploadRateLimit: FileSize = DEFAULT_UPLOAD_RATE_LIMIT,
    /**
     * 种子分享率限制.
     */
    val shareRatioLimit: Double = 1.1,
    @Transient private val _placeholder: Int = 0,
) {
    companion object {
        val DEFAULT_UPLOAD_RATE_LIMIT = 2.megaBytes

        val Default = AnitorrentConfig()
    }
}
