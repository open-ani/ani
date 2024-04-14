package me.him188.ani.datasources.core.cache

import kotlinx.coroutines.flow.Flow
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.core.fetch.MediaFetcher

/**
 * A local storage for caching media.
 *
 * ## Identity
 *
 * [MediaCacheStorage] and [MediaSource] use the same ID system and,
 * there can be a [MediaSource] with the same ID as this [MediaCacheStorage].
 *
 * By having a [MediaSource] with the same ID,
 * a [MediaCacheStorage] can participate in the [MediaFetcher.fetch] process (and usually it should).
 */
interface MediaCacheStorage : AutoCloseable {
    /**
     * ID of this media source.
     */
    val mediaSourceId: String

    /**
     * The [MediaSource] implementation that queries the caches from this storage.
     */
    val cacheMediaSource: MediaSource

    /**
     * Number of caches in this storage.
     */
    val count: Flow<Int>

    /**
     * Total size of the cache.
     */
    val totalSize: Flow<FileSize>

    /**
     * A flow that subscribes on all the caches in the storage.
     *
     * Note that to retrieve [Media] (more specifically, [CachedMedia]) from the cache storage, you might want to use [cacheMediaSource].
     */
    val listFlow: Flow<List<MediaCache>>

    /**
     * Returns the cache of the media if it exists, or `null` if it doesn't.
     */
    suspend fun findCache(media: Media, resume: Boolean = true): MediaCache?

    /**
     * Finds the existing cache for the media or adds the media to the cache (queue).
     *
     * When this function returns, A new [MediaSource] can then be listed by [listFlow].
     *
     * Caching is made asynchronously. This function might only adds a job to the queue and does not guarantee when the cache will be done.
     *
     * This function returns only if the cache configuration is persisted.
     *
     * @param request The request to fetch the media.
     */
    suspend fun cache(media: Media, request: MediaFetchRequest, resume: Boolean = true): MediaCache

    /**
     * Delete the cache if it exists.
     * @return `true` if a cache was deleted, `false` if there wasn't such a cache.
     */
    suspend fun delete(media: Media): Boolean
}

/**
 * A media cached in the storage.
 */
interface MediaCache {
    /**
     * Original media that is being cached.
     */
    val origin: Media

    /**
     * Returns the [CachedMedia] instance for this cache.
     * The instance is cached so this function will immediately return the cached instance after the first successful call.
     */
    suspend fun getCachedMedia(): CachedMedia

    val metadata: MediaCacheMetadata

    /**
     * A flow listens on the progress of the caching. Range is `0..1`
     */
    val progress: Flow<Float>

    val finished: Flow<Boolean>

    /**
     * A flow listens on the total size of the media.
     */
    val totalSize: Flow<FileSize>

    /**
     * Pauses download of this media.
     * Attempts when the cache has already been deleted will be ignored.
     */
    suspend fun pause()

    /**
     * Continue downloading.
     * Attempts when the cache has already been deleted will be ignored.
     */
    suspend fun resume()

//    /**
//     * Opens the cached file as a [SeekableInput].
//     * The returned [SeekableInput] needs to be closed properly.
//     *
//     * When the media is [deleted][MediaCacheStorage.delete] from a storage,
//     * any operation on [SeekableInput] will throw an exception.
//     */
//    suspend fun open(): SeekableInput

    /**
     * Deletes the cache.
     *
     * This function must close every using resources cleanup potential cache files,
     * and must not throw.
     */
    suspend fun delete()
}

/**
 * Returns `true` if the request matches the cache, either exactly or fuzzily.
 */
infix fun MediaFetchRequest.matches(cache: MediaCacheMetadata): MatchKind {
    if (episodeId != null && cache.episodeId != null) {
        // Both query and cache have episodeId, perform exact match.
        if (cache.episodeId == episodeId) {
            return MatchKind.EXACT
        }

        // Don't go for fuzzy match otherwise we'll always get false positives.
        return MatchKind.NONE
    }

    // Exact match is not possible, do a fuzzy match.

    // Success if the episode name exactly matches
    if (episodeName.isNotEmpty() && cache.episodeName == episodeName) return MatchKind.FUZZY

    if (subjectNames.any { cache.subjectNames.contains(it) }) {
        // Any subject name matches

        return if (episodeSort == cache.episodeSort) {
            // Episode sort matches
            MatchKind.FUZZY
        } else {
            MatchKind.NONE
        }
    }

    return MatchKind.NONE
}
