package me.him188.ani.app.data.source.media

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.episode.EpisodeCollection
import me.him188.ani.app.data.models.episode.episode
import me.him188.ani.app.data.models.episode.isKnownCompleted
import me.him188.ani.app.data.models.preference.MediaCacheSettings
import me.him188.ani.app.data.models.subject.SubjectCollection
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.source.media.cache.MediaCacheStorage
import me.him188.ani.app.data.source.media.fetch.MediaFetcher
import me.him188.ani.app.data.source.media.fetch.create
import me.him188.ani.app.data.source.media.selector.MediaSelectorFactory
import me.him188.ani.app.data.source.media.selector.autoSelect
import me.him188.ani.app.tools.caching.ContentPolicy
import me.him188.ani.app.tools.caching.data
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

interface MediaAutoCacheService {
    suspend fun checkCache()

    fun startRegularCheck(scope: CoroutineScope)
}

fun DefaultMediaAutoCacheService.Companion.createWithKoin(
    koin: Koin = GlobalContext.get()
): DefaultMediaAutoCacheService = DefaultMediaAutoCacheService(
    mediaFetcherLazy = koin.get<MediaSourceManager>().mediaFetcher,
    mediaSelectorFactory = MediaSelectorFactory.withKoin(koin),
    subjectCollections = { settings ->
        @Suppress("DEPRECATION")
        koin.get<SubjectManager>()
            .collectionsByType[UnifiedCollectionType.DOING]!!
            .data(ContentPolicy.CACHE_FIRST)
            .filter { it.isNotEmpty() }
            .map { list ->
                list.take(settings.mostRecentCount)
            }
            .first()
    },
    configLazy = flow {
        emitAll(koin.get<SettingsRepository>().mediaCacheSettings.flow)
    },
    epsNotCached = {
        koin.get<SubjectManager>().episodeCollectionsFlow(it).first()
    },
    cacheManager = koin.inject(),
    targetStorage = flow {
        emit(koin.get<MediaCacheManager>())
    }.flatMapLatest { manager ->
        manager.enabledStorages.mapNotNull { it.firstOrNull() }
    },
)

// TODO: refactor the shit DefaultMediaAutoCacheService
class DefaultMediaAutoCacheService(
    private val mediaFetcherLazy: Flow<MediaFetcher>,
    private val mediaSelectorFactory: MediaSelectorFactory,
    /**
     * Emits list of subjects to be considered caching. 通常是 "在看" 分类的. 只需要前几个 (根据配置 [MediaCacheSettings.mostRecentOnly]).
     */
    private val subjectCollections: suspend (MediaCacheSettings) -> List<SubjectCollection>,
    private val configLazy: Flow<MediaCacheSettings>,
    private val epsNotCached: suspend (subjectId: Int) -> List<EpisodeCollection>,
    /**
     * Used to query if a episode already has a cache.
     */
    cacheManager: Lazy<MediaCacheManager>,
    /**
     * Target storage to make caches to. It must be managed by the [MediaCacheManager].
     */
    private val targetStorage: Flow<MediaCacheStorage>,
) : MediaAutoCacheService {
    private val cacheManager by cacheManager

    override suspend fun checkCache() {
        logger.info { "DefaultMediaAutoCacheService.checkCache: start" }

        val config = configLazy.first()
        val collections = subjectCollections(config).run {
            if (config.mostRecentOnly) {
                take(config.mostRecentCount)
            } else this
        }

        logger.info { "checkCache: checking ${collections.size} subjects" }

        for (subject in collections) {
            val firstUnwatched = firstEpisodeToCache(
                eps = epsNotCached(subject.subjectId),
                hasAlreadyCached = {
                    cacheManager.cacheStatusForEpisode(subject.subjectId, it.episode.id)
                        .firstOrNull() != EpisodeCacheStatus.NotCached
                },
                maxCount = config.maxCountPerSubject,
            ).firstOrNull() ?: continue // 都看过了

            logger.info { "Caching ${subject.debugName()} ${firstUnwatched.episode.name}" }
            createCache(subject, firstUnwatched)
            logger.info { "Completed creating cache for ${subject.debugName()} ${firstUnwatched.episode.name}, delay 1 min" }

            delay(1.minutes) // don't fetch too fast from sources
        }

        logger.info { "DefaultMediaAutoCacheService.checkCache: all ${collections.size} subjects checked" }
    }

    private suspend fun createCache(
        subject: SubjectCollection,
        firstUnwatched: EpisodeCollection
    ) {
        val fetcher = mediaFetcherLazy.first()
        val fetchSession = fetcher.newSession(MediaFetchRequest.create(subject.info, firstUnwatched.episode))
        val selector = mediaSelectorFactory.create(
            subject.subjectId,
            fetchSession.cumulativeResults,
        )
        val selected = selector.autoSelect.awaitCompletedAndSelectDefault(fetchSession)
        if (selected == null) {
            logger.info { "No media selected for ${subject.debugName()} ${firstUnwatched.episode.name}" }
            return
        }
        val cache = targetStorage.first().cache(selected, MediaCacheMetadata(fetchSession.request.first()))
        logger.info { "Created cache '${cache.cacheId}' for ${subject.debugName()} ${firstUnwatched.episode.name}" }
    }

    override fun startRegularCheck(scope: CoroutineScope) {
        scope.launch(CoroutineName("MediaAutoCacheService.startRegularCheck")) {
            while (true) {
                val config = configLazy.first()
                if (!config.enabled) {
                    delay(1.hours)
                    continue
                }
                try {
                    checkCache()
                } catch (e: Throwable) {
                    logger.error(e) { "Failed to do regular cache check" }
                }
                delay(1.hours)
            }
        }
    }

    private fun SubjectCollection.debugName() = displayName

    companion object {
        val logger = logger(DefaultMediaAutoCacheService::class)

        /**
         */
        // public for testing
        fun firstEpisodeToCache(
            eps: List<EpisodeCollection>,
            hasAlreadyCached: suspend (EpisodeCollection) -> Boolean,
            maxCount: Int = Int.MAX_VALUE,
        ): Flow<EpisodeCollection> {
            var cachedCount = 0
            return eps
                .asSequence()
                .takeWhile { it.episode.isKnownCompleted }
                .dropWhile {
                    it.collectionType.isDoneOrDropped() // 已经看过的不考虑
                }
                .run {
                    val seq = this
                    flow {
                        for (it in seq) {
                            if (cachedCount >= maxCount) { // 已经缓存了足够多的
                                break
                            }

                            if (!hasAlreadyCached(it)) {
                                emit(it)
                            }
                            cachedCount++
                        }
                    }
                }
        }
    }
}