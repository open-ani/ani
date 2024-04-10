package me.him188.ani.datasources.api.topic

import kotlinx.serialization.Serializable


@Serializable
sealed class ResourceLocation {
    abstract val uri: String

    /**
     * `magnet:?xt=urn:btih:...`
     */
    @Serializable
    class MagnetLink(override val uri: String) : ResourceLocation() {
        init {
            require(uri.startsWith("magnet:")) {
                "MagnetLink uri must start with magnet:"
            }
        }
    }

    /**
     * `*.torrent` form `http://`, `https://`.
     */
    @Serializable
    class HttpTorrentFile(override val uri: String) : ResourceLocation() {
        init {
            require(uri.startsWith("https://") || uri.startsWith("http://")) {
                "HttpTorrentFile uri must start with http:// or https://"
            }
        }
    }

    /**
     * `file://`
     */
    @Serializable
    class LocalFile(
        override val uri: String
    ) : ResourceLocation() {
        val filePath: String
            get() = uri.removePrefix("file://")

        init {
            require(uri.startsWith("file://")) {
                "LocalFile uri must start with file://"
            }
        }
    }
}
