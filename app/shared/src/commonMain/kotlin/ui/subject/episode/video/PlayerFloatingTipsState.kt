package me.him188.ani.app.ui.subject.episode.video

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.him188.ani.app.videoplayer.ui.state.Chapter

@Stable
class PlayerFloatingTipsState {
    var leftBottomTipsVisible: Boolean by mutableStateOf(false)
    var skipOpEd: Boolean by mutableStateOf(true)

    fun cancelSkipOpEd() {
        leftBottomTipsVisible = false
        skipOpEd = false
    }

    fun autoSkipOpEd(pos: Long, max: Long?, chapters: List<Chapter>, onSeek: (Long) -> Unit) {
        // 已经点击过取消跳过 OP 或 ED时本集不再出现提示
        if (max == null || !skipOpEd) return

        chapters.forEach {
            val matched = when {
                max > 20 * 60 * 1000 -> {
                    it.durationMillis in 85_000..95_000
                }

                max > 10 * 60 * 1000 -> {
                    it.durationMillis in 55_000..65_000
                }

                else -> {
                    false
                }
            }
            if (matched && (pos + 5000 - it.offsetMillis) in 0..1000) {
                leftBottomTipsVisible = true
            }
            if (matched && (pos - it.offsetMillis) in 0..1000) {
                leftBottomTipsVisible = false
                if (!skipOpEd) {
                    return@forEach
                }
                onSeek(it.offsetMillis + it.durationMillis)
            }
        }
    }
}
