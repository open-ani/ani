package me.him188.ani.app.data.source.media.selector

import kotlinx.coroutines.flow.debounce
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import kotlin.jvm.JvmInline

inline val MediaSelector.eventHandling get() = MediaSelectorEventHandlers(this)

@JvmInline
value class MediaSelectorEventHandlers(
    private val mediaSelector: MediaSelector,
) {
    /**
     * 保存本次会话用户更新的资源选择偏好设置
     */
    suspend fun savePreferenceOnSelect(
        save: suspend (MediaPreference) -> Unit,
    ) {
        mediaSelector.events.onChangePreference.debounce(1000).collect {
            save(it)
        }
    }
}
