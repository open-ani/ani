package me.him188.ani.app.data.source.media.selector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.him188.ani.app.data.source.media.instance.MediaSourceInstance
import kotlin.jvm.JvmName

data class MediaSelectorContext(
    /**
     * 该条目已经完结了一段时间了. `null` 表示该信息还正在查询中
     */
    val subjectFinished: Boolean?,
    /**
     * 在执行自动选择时, 需要按此顺序使用数据源.
     * 为 `null` 表示无偏好, 可以按任意顺序选择.
     *
     * 当使用完所有偏好的数据源后都没有筛选到资源时, 将会 fallback 为选择任意数据源的资源
     */
    val mediaSourcePrecedence: List<String>?,
    /**
     * 用于针对各个平台的播放器缺陷，调整选择资源的优先级
     */
    val subtitlePreferences: MediaSelectorSubtitlePreferences?,
) {
    fun allFieldsLoaded() = subjectFinished != null
            && mediaSourcePrecedence != null
            && subtitlePreferences != null

    companion object {
        /**
         * 刚开始查询时的默认值
         */
        val Initial = MediaSelectorContext(null, null, null)

        val EmptyForPreview get() = MediaSelectorContext(false, emptyList(), null)
    }
}

/**
 * 便捷地根据 flow 参数创建一个 flow [MediaSelectorContext].
 */
fun MediaSelectorContext.Companion.createFlow(
    subjectCompleted: Flow<Boolean>,
    mediaSourcePrecedence: Flow<List<String>>,
    subtitleKindFilters: Flow<MediaSelectorSubtitlePreferences>,
): Flow<MediaSelectorContext> = combine(
    subjectCompleted, mediaSourcePrecedence, subtitleKindFilters,
) { completed, instances, filters ->
    MediaSelectorContext(
        subjectFinished = completed,
        mediaSourcePrecedence = instances,
        subtitlePreferences = filters,
    )
}.onStart {
    emit(Initial) // 否则如果一直没获取到剧集信息, 就无法选集, #385
}

/**
 * 便捷地根据 flow 参数创建一个 flow [MediaSelectorContext].
 */
@JvmName("createFlow2")
fun MediaSelectorContext.Companion.createFlow(
    subjectCompleted: Flow<Boolean>,
    mediaSourcePrecedence: Flow<List<MediaSourceInstance>>,
    subtitleKindFilters: Flow<MediaSelectorSubtitlePreferences>,
): Flow<MediaSelectorContext> = createFlow(
    subjectCompleted,
    mediaSourcePrecedence.map { list ->
        list.map { it.mediaSourceId }
    },
    subtitleKindFilters,
)
