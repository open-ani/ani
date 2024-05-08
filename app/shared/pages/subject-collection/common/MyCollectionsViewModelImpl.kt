package me.him188.ani.app.ui.collection

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.ViewModelAuthSupport
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.subject.SubjectCollectionItem
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.data.subject.setEpisodeWatched
import me.him188.ani.app.tools.caching.ContentPolicy
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.tools.caching.getCachedData
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
interface MyCollectionsViewModel : HasBackgroundScope, ViewModelAuthSupport {
    val subjectManager: SubjectManager

    @Stable
    fun collectionsByType(type: UnifiedCollectionType): LazyDataCache<SubjectCollectionItem>

    /**
     * 返回用户观看该番剧的进度 [Flow].
     *
     * 该 flow 总是使用 [ContentPolicy.CACHE_ONLY]
     */
    @Stable
    fun subjectProgress(subjectId: Int): Flow<List<EpisodeProgressItem>>

    @Stable
    fun cacheStatusForEpisode(subjectId: Int, episodeId: Int): Flow<EpisodeCacheStatus>

    suspend fun setCollectionType(subjectId: Int, type: UnifiedCollectionType)

    suspend fun setAllEpisodesWatched(subjectId: Int)

    suspend fun setEpisodeWatched(subjectId: Int, episodeId: Int, watched: Boolean)
}

fun MyCollectionsViewModel(): MyCollectionsViewModel = MyCollectionsViewModelImpl()

@Stable
class MyCollectionsViewModelImpl : AbstractViewModel(), KoinComponent, MyCollectionsViewModel {
    override val subjectManager: SubjectManager by inject()
    private val cacheManager: MediaCacheManager by inject()

    val collectionsByType get() = subjectManager.collectionsByType

    @Stable
    override fun collectionsByType(type: UnifiedCollectionType) =
        subjectManager.collectionsByType[type]!!

    @Stable
    override fun subjectProgress(subjectId: Int): Flow<List<EpisodeProgressItem>> =
        subjectManager.subjectProgressFlow(subjectId, ContentPolicy.CACHE_ONLY)

    @Stable
    override fun cacheStatusForEpisode(subjectId: Int, episodeId: Int): Flow<EpisodeCacheStatus> =
        cacheManager.cacheStatusForEpisode(
            subjectId = subjectId,
            episodeId = episodeId,
        )

    override suspend fun setCollectionType(subjectId: Int, type: UnifiedCollectionType) =
        subjectManager.setSubjectCollectionType(subjectId, type)

    override suspend fun setAllEpisodesWatched(subjectId: Int) = subjectManager.setAllEpisodesWatched(subjectId)

    override suspend fun setEpisodeWatched(subjectId: Int, episodeId: Int, watched: Boolean) =
        subjectManager.setEpisodeWatched(subjectId, episodeId, watched)

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
                collectionsByType[type]?.let { cache ->
                    if (cache.getCachedData().isEmpty()) {
                        cache.requestMore()
                    }
                }
            }
        }
    }
}

sealed class ContinueWatchingStatus {
    data object Start : ContinueWatchingStatus()

    /**
     * 还未开播
     */
    data object NotOnAir : ContinueWatchingStatus()

    /**
     * 继续看
     */
    class Continue(
        val episodeIndex: Int,
        val episodeSort: String, // "12.5"
    ) : ContinueWatchingStatus()

    /**
     * 看到了, 但是下一集还没更新
     */
    class Watched(
        val episodeIndex: Int,
        val episodeSort: String, // "12.5"
    ) : ContinueWatchingStatus()

    data object Done : ContinueWatchingStatus()
}

