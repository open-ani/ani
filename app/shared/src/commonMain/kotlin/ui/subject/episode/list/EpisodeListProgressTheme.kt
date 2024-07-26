package me.him188.ani.app.ui.subject.episode.list

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
enum class EpisodeListProgressTheme {
    /**
     * 点亮模式, 看过的是亮色
     */
    LIGHT_UP,

    /**
     * 动作模式, 可以看的是亮色
     */
    ACTION;

    companion object {
        @Stable
        val Default = ACTION
    }
}