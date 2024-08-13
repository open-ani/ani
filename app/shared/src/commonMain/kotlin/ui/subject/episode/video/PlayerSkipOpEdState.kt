package me.him188.ani.app.ui.subject.episode.video

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.him188.ani.app.videoplayer.ui.state.Chapter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Stable
class PlayerSkipOpEdState(
    chapters: State<List<Chapter>>,
    private val onSkip: (targetMillis: Long) -> Unit,
    videoLength: State<Duration>,
) {
    private var currentChapter: CurrentChapter? by mutableStateOf(null) 
    private val opEdChapters by derivedStateOf {
        chapters.value.filter {
            OpEdLength.fromVideoLengthOrNull(videoLength.value)
                ?.isOpEdChapter(it.durationMillis.milliseconds) == true
        }
    }

    val skipped: Boolean by derivedStateOf {
        currentChapter?.skipped ?: false
    }
    val showSkipTips: Boolean by derivedStateOf {
        currentChapter != null && skipped
    }

    fun cancelSkipOpEd() {
        currentChapter = currentChapter?.copy(skipped = false)
        println(currentChapter?.skipped)
    }

    fun resetSkipOpEd() {
        currentChapter = null
    }

    /**
     * 每秒调用一次update
     * 根据[currentPos]感知[currentPos]到5秒后这个区间是否会有章节开头，
     * 根据当前秒的位置显示/隐藏tips，
     * 并且如果[currentPos]在章节开头的位置，根据[skipped]跳过该章节
     */
    fun update(currentPos: Long) {
        if (opEdChapters.isEmpty()) return
        // 在显示跳过提示范围
        opEdChapters.find { it.offsetMillis in currentPos - 1000..currentPos + 5000 }?.let {
            if (currentChapter == null) {
                currentChapter = CurrentChapter(it, true)
            }
        } ?: run {
            currentChapter = null
        }
        // 在跳过 OP/ED 范围
        opEdChapters.find { it.offsetMillis in currentPos - 1000..currentPos }?.run {
            if (currentChapter?.skipped == false) return
            onSkip(offsetMillis + durationMillis)
            currentChapter = null
        }
    }
}

data class CurrentChapter(val chapter: Chapter, var skipped: Boolean)

fun interface OpEdLength {
    fun isOpEdChapter(chapterLength: Duration): Boolean

    companion object {
        private val Normal = OpEdLength { it in 85.seconds..95.seconds }
        private val Short = OpEdLength { it in 55.seconds..65.seconds }

        fun fromVideoLengthOrNull(length: Duration): OpEdLength? {
            return when {
                length > 20.minutes -> Normal
                length > 10.minutes -> Short
                else -> null
            }
        }
    }
}
