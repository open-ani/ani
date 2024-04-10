package me.him188.ani.app.data.media

import me.him188.ani.datasources.core.cache.MediaCacheStorage

interface MediaCacheManager {
    val storages: List<MediaCacheStorage>
}

class MediaCacheManagerImpl(override val storages: List<MediaCacheStorage>) : MediaCacheManager