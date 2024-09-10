package me.him188.ani.app.ui.subject.collection

import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.models.episode.type
import me.him188.ani.app.data.models.preference.MyCollectionsSettings
import me.him188.ani.app.data.models.subject.SubjectCollection
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.source.media.cache.EpisodeCacheStatus
import me.him188.ani.app.data.source.media.cache.MediaCacheManager
import me.him188.ani.app.data.source.session.AuthState
import me.him188.ani.app.data.source.session.SessionEvent
import me.him188.ani.app.data.source.session.SessionManager
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.tools.caching.RefreshOrderPolicy
import me.him188.ani.app.tools.caching.getCachedData
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.collection.progress.EpisodeListStateFactory
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressStateFactory
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.platform.currentTimeMillis
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.minutes

@Stable
class CollectionsByType(
    val type: UnifiedCollectionType,
    val cache: LazyDataCache<SubjectCollection>,
    val subjectCollectionColumnState: SubjectCollectionColumnState,
) {
    val pullToRefreshState: PullToRefreshState = PullToRefreshState()

    suspend fun shouldDoAutoRefresh(): Boolean {
        val lastUpdated = cache.lastUpdated.first()
        return currentTimeMillis() - lastUpdated > 60.minutes.inWholeMilliseconds
    }
}

@Stable
class MyCollectionsViewModel : AbstractViewModel(), KoinComponent {
    lateinit var navigator: AniNavigator

    private val subjectManager: SubjectManager by inject()
    private val cacheManager: MediaCacheManager by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val sessionManager: SessionManager by inject()

    val collectionsByType = subjectManager.collectionsByType.map { (type, cache) ->
        CollectionsByType(
            type, cache,
            SubjectCollectionColumnState(
                cachedData = cache.cachedDataFlow.produceState(emptyList()),
                hasMore = cache.isCompleted.map { !it }.produceState(true),
                isKnownAuthorizedAndEmpty = combine(cache.isCompleted, cache.cachedDataFlow) { completed, data ->
                    completed && data.isEmpty()
                }.produceState(false),
                onRequestMore = { cache.requestMore() },
                onAutoRefresh = { cache.refresh(RefreshOrderPolicy.REPLACE) },
                onManualRefresh = { cache.refresh(RefreshOrderPolicy.KEEP_ORDER_APPEND_LAST) },
                backgroundScope,
            ),
        )
    }

    val myCollectionsSettings: MyCollectionsSettings by settingsRepository.uiSettings.flow
        .map { it.myCollections }
        .produceState(MyCollectionsSettings.Default)
    val authState: AuthState = AuthState()
    val episodeListStateFactory: EpisodeListStateFactory = EpisodeListStateFactory(
        settingsRepository,
        subjectManager,
        backgroundScope,
    )
    val subjectProgressStateFactory: SubjectProgressStateFactory = SubjectProgressStateFactory(
        subjectManager,
        onPlay = { subjectId: Int, episodeId ->
            navigator.navigateEpisodeDetails(subjectId, episodeId)
        },
    )

    fun createEditableSubjectCollectionTypeState(subjectCollection: SubjectCollection): EditableSubjectCollectionTypeState =
        // 必须不能有后台持续任务
        EditableSubjectCollectionTypeState(
            selfCollectionType = stateOf(subjectCollection.collectionType),
            hasAnyUnwatched = hasAnyUnwatched@{
                val collections = subjectManager.episodeCollectionsFlow(subjectCollection.subjectId)
                    .firstOrNull() ?: return@hasAnyUnwatched true
                collections.any { !it.type.isDoneOrDropped() }
            },
            onSetSelfCollectionType = {
                subjectManager.setSubjectCollectionType(subjectCollection.subjectId, it)
            },
            onSetAllEpisodesWatched = {
                subjectManager.setAllEpisodesWatched(subjectCollection.subjectId)
            },
            backgroundScope,
        )

    @Stable
    fun collectionsByType(type: UnifiedCollectionType): CollectionsByType =
        collectionsByType.firstOrNull { it.type == type }
            ?: error("No such collection type: $type") // should not happen


    private val requestMoreTaskers = collectionsByType.map { it.type }.associateWith {
        MonoTasker(backgroundScope)
    }

    fun requestMore(type: UnifiedCollectionType) {
        requestMoreTaskers[type]!!.run {
            if (isRunning) return
            launch {
                collectionsByType(type).cache.requestMore()
            }
        }
    }

    @Stable
    fun cacheStatusForEpisode(subjectId: Int, episodeId: Int): Flow<EpisodeCacheStatus> =
        cacheManager.cacheStatusForEpisode(
            subjectId = subjectId,
            episodeId = episodeId,
        )

    suspend fun setCollectionType(subjectId: Int, type: UnifiedCollectionType) =
        subjectManager.setSubjectCollectionType(subjectId, type)

    suspend fun setAllEpisodesWatched(subjectId: Int) = subjectManager.setAllEpisodesWatched(subjectId)

    override fun init() {
        // 获取第一页, 得到数量
        // 不要太快, 测试到的如果全并行就会导致 "在看" 没有数据, 不清楚是哪边问题.
        launchInBackground {
            // 按实用顺序加载
            listOf(
                UnifiedCollectionType.DOING,
                UnifiedCollectionType.WISH,
                UnifiedCollectionType.ON_HOLD,
                UnifiedCollectionType.DONE,
                UnifiedCollectionType.DROPPED,
            ).forEach { type ->
                collectionsByType(type).cache.let { cache ->
                    if (cache.getCachedData().isEmpty()) {
                        cache.requestMore()
                    }
                }
            }
        }

        launchInBackground {
            sessionManager.events.filter {
                when (it) {
                    SessionEvent.SwitchToGuest -> false
                    SessionEvent.TokenRefreshed -> false
                    SessionEvent.Login -> true
                    SessionEvent.Logout -> true
                }
            }.collectLatest {
                logger.info { "登录信息变更, 清空缓存" }
                // 如果有变更登录, 清空缓存
                for (collections in collectionsByType) {
                    collections.cache.invalidate()
                }
            }
        }
    }
}
