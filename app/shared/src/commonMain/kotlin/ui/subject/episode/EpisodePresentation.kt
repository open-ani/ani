package me.him188.ani.app.ui.subject.episode

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import me.him188.ani.app.data.model.episode.EpisodeCollection
import me.him188.ani.app.data.model.episode.EpisodeInfo
import me.him188.ani.app.data.model.episode.displayName
import me.him188.ani.app.data.model.episode.episode
import me.him188.ani.app.data.model.episode.isKnownCompleted
import me.him188.ani.app.data.model.episode.renderEpisodeEp
import me.him188.ani.app.data.model.subject.nameCnOrName
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

/**
 * 展示在 UI 的状态
 */
@Immutable
class EpisodePresentation(
    val episodeId: Int,
    /**
     * 剧集标题
     * @see EpisodeInfo.nameCnOrName
     */
    val title: String,
    /**
     * 在当前季度中的集数, 例如第二季的第一集为 01
     *
     * @see renderEpisodeEp
     */
    val ep: String,
    /**
     * 在系列中的集数, 例如第二季的第一集为 26
     *
     * @see renderEpisodeEp
     */
    val sort: String,
    val collectionType: UnifiedCollectionType,
    /**
     * 是否已经确定开播了
     */
    val isKnownBroadcast: Boolean,
    val isPlaceholder: Boolean = false,
) {
    companion object {
        @Stable
        val Placeholder = EpisodePresentation(
            episodeId = -1,
            title = "placeholder",
            ep = "placeholder",
            sort = "placeholder",
            collectionType = UnifiedCollectionType.WISH,
            isKnownBroadcast = false,
            isPlaceholder = true,
        )
    }
}

fun EpisodeCollection.toPresentation() = EpisodePresentation(
    episodeId = this.episode.id,
    title = episode.displayName,
    ep = episode.renderEpisodeEp(),
    sort = episode.sort.toString(),
    collectionType = collectionType,
    isKnownBroadcast = episode.isKnownCompleted,
)
