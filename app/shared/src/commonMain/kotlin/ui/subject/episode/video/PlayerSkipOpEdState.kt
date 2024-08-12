package me.him188.ani.app.ui.subject.episode.video

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import me.him188.ani.app.videoplayer.ui.state.Chapter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Stable
class PlayerSkipOpEdState(
    chapters: State<List<Chapter>>,
    private val onSkip: (targetMillis: Long) -> Unit,
    videoLength: State<Duration>,
) {
    private val opEdChapters by derivedStateOf {
        chapters.value.filter {
            OpEdLength.fromVideoLengthOrNull(videoLength.value)
                ?.isOpEdChapter(it.durationMillis.toDuration(DurationUnit.MILLISECONDS)) == true
        }
    }
    private val skipCancelRequests = SnapshotStateList<Any>()
    private fun requestSkipCancel(requester: Any, cancel: Boolean) {
        if (cancel) {
            if (skipCancelRequests.contains(requester)) return
            skipCancelRequests.add(requester)
        } else {
            skipCancelRequests.remove(requester)
        }
    }

    val skipCancel by derivedStateOf {
        skipCancelRequests.isNotEmpty()
    }
    private val showSkipTipsRequests = SnapshotStateList<Any>()
    private fun requestShowSkipTips(requester: Any, show: Boolean) {
        if (show) {
            if (showSkipTipsRequests.contains(requester)) return
            showSkipTipsRequests.add(requester)
        } else {
            showSkipTipsRequests.remove(requester)
        }
    }

    val showSkipTips by derivedStateOf {
        showSkipTipsRequests.isNotEmpty()
    }

    fun cancelSkipOpEd() {
        requestSkipCancel(this, true)
        requestShowSkipTips(this, false)
    }

    fun resetSkipOpEd() {
        skipCancelRequests.clear()
    }

    /**
     * 每秒调用一次update
     * 根据[currentPos]感知[currentPos]到5秒后这个区间是否会有章节开头，
     * 根据当前秒的位置显示/隐藏tips，
     * 并且如果[currentPos]在章节开头的位置，根据[skipCancel]跳过该章节
     */
    fun update(currentPos: Long) {
        if (opEdChapters.isEmpty()) return
        // 在跳过 OP/ED 范围
        opEdChapters.find { it.offsetMillis in currentPos - 1000..currentPos }?.run {
            if (skipCancel) return
            onSkip(offsetMillis + durationMillis)
            return
        }
        // 在显示跳过提示范围
        opEdChapters.find { it.offsetMillis in currentPos..currentPos + 5000 }.run {
            if (skipCancel) return
            if (this == null) requestShowSkipTips(this@PlayerSkipOpEdState, false)
            else requestShowSkipTips(this@PlayerSkipOpEdState, true)
        }
    }
}

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
