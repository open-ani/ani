/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.cache.engine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.domain.media.cache.MediaCache
import me.him188.ani.app.domain.media.cache.MediaCacheState
import me.him188.ani.app.tools.toProgress
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext

/**
 * 不会实际发起下载, 内部维护一个虚拟进度条, 用于测试.
 */
class DummyMediaCacheEngine(
    private val mediaSourceId: String,
    private val location: MediaSourceLocation = MediaSourceLocation.Local,
) : MediaCacheEngine {
    override val stats: Flow<MediaStats> = flowOf(MediaStats.Unspecified)

    override fun supports(media: Media): Boolean = true

    override suspend fun restore(
        origin: Media,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache = DummyMediaCache(origin, metadata, mediaSourceId, location)

    override suspend fun createCache(
        origin: Media,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache = DummyMediaCache(origin, metadata, mediaSourceId, location)

    override suspend fun deleteUnusedCaches(all: List<_root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache>) {
    }
}

class DummyMediaCache(
    override val origin: Media,
    override val metadata: MediaCacheMetadata,
    val mediaSourceId: String,
    val location: MediaSourceLocation = MediaSourceLocation.Local,
) : _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache {
    private val cachedMedia by lazy {
        CachedMedia(origin, mediaSourceId, origin.download, location)
    }
    override val state: MutableStateFlow<_root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCacheState> = MutableStateFlow(
        _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCacheState.IN_PROGRESS)

    override suspend fun getCachedMedia(): CachedMedia = cachedMedia

    @Volatile
    private var isValid = true
    override fun isValid(): Boolean = isValid

    override val fileStats: Flow<_root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache.FileStats> =
        flowOf(_root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache.FileStats(300.megaBytes, 100.megaBytes))
    override val sessionStats: Flow<_root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache.SessionStats> =
        flowOf(
            _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache.SessionStats(
                0.megaBytes,
                0.megaBytes,
                0.megaBytes,
                0.megaBytes,
                0.megaBytes,
                0f.toProgress(),
            ),
        )

    override suspend fun pause() {
    }

    override suspend fun close() {
    }

    override suspend fun resume() {
    }

    override val isDeleted: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun closeAndDeleteFiles() {
        isDeleted.value = true
    }
}
