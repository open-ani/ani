package me.him188.ani.app.data.media

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.MediaCacheSettings
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.subject.SubjectCollectionItem
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.tools.caching.ContentPolicy
import me.him188.ani.app.ui.subject.episode.mediaFetch.EpisodeMediaFetchSession
import me.him188.ani.app.ui.subject.episode.mediaFetch.FetcherMediaSelectorConfig
import me.him188.ani.app.ui.subject.episode.mediaFetch.awaitCompletion
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import me.him188.ani.datasources.bangumi.processing.isOnAir
import me.him188.ani.datasources.bangumi.processing.toCollectionType
import me.him188.ani.datasources.core.cache.MediaCacheStorage
import me.him188.ani.utils.coroutines.OwnedCancellationException
import me.him188.ani.utils.coroutines.cancellableCoroutineScope
import me.him188.ani.utils.coroutines.checkOwner
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.openapitools.client.models.EpType
import org.openapitools.client.models.UserEpisodeCollection
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

interface MediaAutoCacheService {
    suspend fun checkCache()

    fun startRegularCheck(scope: CoroutineScope)
}

fun DefaultMediaAutoCacheService(
    koin: Koin = GlobalContext.get()
) = DefaultMediaAutoCacheService(
    subjectCollections = { settings ->
        koin.get<SubjectManager>()
            .subjectCollectionsFlow(ContentPolicy.CACHE_FIRST)
            .map { list ->
                list.asSequence()
                    .filter { it.collectionType == UnifiedCollectionType.DOING }
                    .take(settings.mostRecentCount)
                    .toList()
            }
            .first()
    },
    config = koin.get<SettingsRepository>().mediaCacheSettings.flow,
    episodeRepository = koin.get(),
    cacheManager = koin.get(),
    targetStorage = koin.get<MediaCacheManager>().enabledStorages.mapNotNull { it.firstOrNull() },
)

class DefaultMediaAutoCacheService(
    /**
     * Emits list of subjects to be considered caching. 通常是 "在看" 分类的. 只需要前几个 (根据配置 [MediaCacheSettings.mostRecentOnly]).
     */
    private val subjectCollections: suspend (MediaCacheSettings) -> List<SubjectCollectionItem>,
    private val config: Flow<MediaCacheSettings>,
    private val episodeRepository: EpisodeRepository,
    /**
     * Used to query if a episode already has a cache.
     */
    private val cacheManager: MediaCacheManager,
    /**
     * Target storage to make caches to. It must be managed by the [MediaCacheManager].
     */
    private val targetStorage: Flow<MediaCacheStorage>,
) : MediaAutoCacheService {
    override suspend fun checkCache() {
        logger.info { "DefaultMediaAutoCacheService.checkCache: start" }

        val config = config.first()
        val collections = subjectCollections(config).run {
            if (config.mostRecentOnly) {
                take(config.mostRecentCount)
            } else this
        }

        logger.info { "checkCache: checking ${collections.size} subjects" }


        for (subject in collections) {
            val firstUnwatched = firstEpisodeToCache(
                eps = episodeRepository.getSubjectEpisodeCollection(subject.subjectId, EpType.MainStory),
                hasAlreadyCached = {
                    cacheManager.cacheStatusForEpisode(subject.subjectId, it.episode.id)
                        .firstOrNull() != EpisodeCacheStatus.NotCached
                },
                maxCount = config.maxCountPerSubject,
            ).firstOrNull() ?: continue // 都看过了

            logger.info { "Caching ${subject.debugName()} ${firstUnwatched.episode.name}" }

            cancellableCoroutineScope {
                EpisodeMediaFetchSession(
                    subject.subjectId,
                    firstUnwatched.episode.id,
                    parentCoroutineContext = this.coroutineContext,
                    config = FetcherMediaSelectorConfig.NoSave,
                ).run {
                    this.awaitCompletion()
                    val request = this.mediaFetchSession.first().request
                    this.mediaSelectorState.makeDefaultSelection()
                    this.mediaSelectorState.selected?.let { media ->
                        targetStorage.first().cache(media, MediaCacheMetadata(request))
                        logger.info { "Created cache ${media.mediaId} for ${subject.debugName()} ${firstUnwatched.episode.name}" }
                    }
                }
                cancelScope()
            }

            logger.info { "Completed creating cache for ${subject.debugName()} ${firstUnwatched.episode.name}, delay 1 min" }

            delay(1.minutes) // don't fetch too fast from sources
        }

        logger.info { "DefaultMediaAutoCacheService.checkCache: all ${collections.size} subjects checked" }
    }

    override fun startRegularCheck(scope: CoroutineScope) {
        scope.launch(CoroutineName("MediaAutoCacheService.startRegularCheck")) {
            while (true) {
                val config = config.first()
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

    private fun SubjectCollectionItem.debugName() = displayName

    internal companion object {
        val logger = logger(DefaultMediaAutoCacheService::class)

        // public for testing
        fun firstEpisodeToCache(
            eps: Flow<UserEpisodeCollection>,
            hasAlreadyCached: suspend (UserEpisodeCollection) -> Boolean,
            maxCount: Int = Int.MAX_VALUE,
        ): Flow<UserEpisodeCollection> {
            var cachedCount = 0
            return eps.takeWhile { it.episode.isOnAir() != true } // 还没开播的不考虑
                .dropWhile {
                    it.type.toCollectionType().isDoneOrDropped() // 已经看过的不考虑
                }
                .run {
                    flow {
                        val owner = Any()
                        try {
                            collect {
                                if (cachedCount >= maxCount) { // 已经缓存了足够多的
                                    throw OwnedCancellationException(owner)
                                }

                                if (!hasAlreadyCached(it)) {
                                    emit(it)
                                }
                                cachedCount++
                            }
                        } catch (e: OwnedCancellationException) {
                            e.checkOwner(owner)
                        }
                    }
                }
        }
    }
}