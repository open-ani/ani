package me.him188.ani.datasources.api.topic


sealed class ResourceLocation {
    abstract val uri: String

    /**
     * `magnet:?xt=urn:btih:...`
     */
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
    class HttpTorrentFile(override val uri: String) : ResourceLocation() {
        init {
            require(uri.startsWith("https://") || uri.startsWith("http://")) {
                "HttpTorrentFile uri must start with http:// or https://"
            }
        }
    }

//    /**
//     * `*.mp4`, `*.mkv`, etc.
//     */
//    class HttpVideoFile(override val uri: String) : ResourceLocation() {
//        init {
//            require(uri.startsWith("https://") || uri.startsWith("http://")) {
//                "HttpVideoFile uri must start with http:// or https://"
//            }
//        }
//    }
}
