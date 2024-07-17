package me.him188.ani.datasources.jellyfin

import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceParameters
import me.him188.ani.datasources.api.source.MediaSourceParametersBuilder
import me.him188.ani.datasources.api.source.get

class EmbyMediaSource(config: MediaSourceConfig) : BaseMediaSource(config) {
    companion object {
        const val ID = "emby"
    }

    object Parameters : MediaSourceParametersBuilder() {
        val baseUrl = string(
            "baseUrl",
            default = "http://localhost:8096",
            description = "服务器地址\n示例: http://localhost:8096",
        )
        val userId = string(
            "userId",
            description = "User ID, 可在 Emby \"控制台 - 用户\" 中选择一个用户, 在浏览器地址栏找到 \"userId=\" 后面的内容\n示例: cc91f58d951648829c90115520f6adec",
        )
        val apikey = string(
            "apikey",
            description = "API Key, 可在 Emby \"控制台 - API 秘钥\" 中添加\n示例: b7292a71d51a6bf3a31036086a6d2e23",
        )
    }

    class Factory : MediaSourceFactory {
        override val mediaSourceId: String get() = ID
        override val parameters: MediaSourceParameters = Parameters.build()
        override val allowMultipleInstances: Boolean get() = true
        override fun create(config: MediaSourceConfig): MediaSource = EmbyMediaSource(config)
    }

    override val kind: MediaSourceKind get() = MediaSourceKind.WEB
    override val mediaSourceId: String get() = ID
    override val baseUrl = config[Parameters.baseUrl].removeSuffix("/")
    override val userId = config[Parameters.userId]
    override val apiKey = config[Parameters.apikey]

    override fun getDownloadUri(itemId: String): String {
        return "$baseUrl/Items/$itemId/Download?api_key=$apiKey"
    }
}