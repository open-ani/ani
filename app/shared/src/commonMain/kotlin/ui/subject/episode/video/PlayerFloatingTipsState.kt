package me.him188.ani.app.ui.subject.episode.video

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.him188.ani.app.videoplayer.ui.state.Chapter

@Stable
class PlayerFloatingTipsState {
    /**
     * 显示 "即将跳过 OP/ED" 的提示, 通常在播放器左下角
     */
    var showSkipOpEd: Boolean by mutableStateOf(false)
        private set

    /**
     * 允许跳过 OP/ED
     */
    var enableSkipOpEd: Boolean by mutableStateOf(true)

    /**
     * 取消跳过 OP/ED 并隐藏提示
     */
    fun cancelSkipOpEd() {
        showSkipOpEd = false
        enableSkipOpEd = false
    }

    fun calculateTargetTime(chapters: List<Chapter>, pos: Long, max: Long): Long? {

        // 已经点击过取消跳过 OP 或 ED时本集不再出现提示
        if (!enableSkipOpEd) return null
        chapters.forEach { chapter ->
            val matched = when {
                max > 20 * 60 * 1000 -> chapter.durationMillis in 85_000..95_000
                max > 10 * 60 * 1000 -> chapter.durationMillis in 55_000..65_000
                else -> false
            }

            if (matched && (pos + 5000 - chapter.offsetMillis) in 0..1000) {
                showSkipOpEd = true
            }
            if (matched && (pos - chapter.offsetMillis) in 0..1000) {
                showSkipOpEd = false
                return chapter.offsetMillis + chapter.durationMillis
            }
        }
        return null
    }
}
