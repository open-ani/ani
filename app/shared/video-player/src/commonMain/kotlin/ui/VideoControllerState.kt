package me.him188.ani.app.videoplayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList


/**
 * @param initialVisible 变更不会更新
 */
@Composable
fun rememberVideoControllerState(
    initialVisible: Boolean = false
): VideoControllerState {
    return remember {
        VideoControllerState(initialVisible)
    }
}

@Stable
class VideoControllerState(
    initialVisible: Boolean = false
) {
    /**
     * 控制器是否可见.
     */
    var isVisible: Boolean by mutableStateOf(initialVisible)
    val setVisible: (Boolean) -> Unit = { isVisible = it }

    fun toggleVisible(desired: Boolean? = null) {
        isVisible = desired ?: !isVisible
    }

    var danmakuEnabled by mutableStateOf(true)
    fun toggleDanmakuEnabled() {
        danmakuEnabled = !danmakuEnabled
    }

    private val alwaysOnRequests = SnapshotStateList<Any>()

    /**
     * 总是显示. 也就是不要在 5 秒后自动隐藏.
     */
    val alwaysOn: Boolean by derivedStateOf {
        alwaysOnRequests.isNotEmpty()
    }

    /**
     * 请求控制器总是显示.
     */
    fun setRequestAlwaysOn(requester: Any, isAlwaysOn: Boolean) {
        if (isAlwaysOn) {
            alwaysOnRequests.add(requester)
        } else {
            alwaysOnRequests.remove(requester)
        }
    }
}
