package me.him188.ani.app.videoplayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList


/**
 * @param initialVisibility 变更不会更新
 */
@Composable
fun rememberVideoControllerState(
    initialVisibility: ControllerVisibility = ControllerVisibility.Invisible
): VideoControllerState {
    return remember {
        VideoControllerState(initialVisibility)
    }
}

@Immutable
data class ControllerVisibility(
    val topBar: Boolean,
    val bottomBar: Boolean,
    val floatingBottomEnd: Boolean,
    val rhsBar: Boolean,
    val detachedSlider: Boolean
) {
    companion object {
        val Visible = ControllerVisibility(
            topBar = true,
            bottomBar = true,
            floatingBottomEnd = false,
            rhsBar = true,
            detachedSlider = false,
        )
        val Invisible = ControllerVisibility(
            topBar = false,
            bottomBar = false,
            floatingBottomEnd = true,
            rhsBar = false,
            detachedSlider = false,
        )
        val DetachedSliderOnly = ControllerVisibility(
            topBar = false,
            bottomBar = true,
            floatingBottomEnd = false,
            rhsBar = false,
            detachedSlider = true,
        )
    }
}
@Stable
class VideoControllerState(
    initialVisibility: ControllerVisibility = ControllerVisibility.Visible
) {
    /**
     * 控制器是否可见.
     */
    var visibility: ControllerVisibility by mutableStateOf(initialVisibility)
    val setVisibility: (ControllerVisibility) -> Unit = {
        visibility = it
    }

    fun toggleVisibility(desired: ControllerVisibility? = null) {
        setVisibility(
            desired
                ?: if (visibility == ControllerVisibility.Visible) ControllerVisibility.Invisible else ControllerVisibility.Visible,
        )
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
    fun setRequestProgressBarVisible() {
        if (!visibility.bottomBar) {
            visibility = ControllerVisibility.DetachedSliderOnly
        }
    }

    fun cancelRequestProgressBarVisible() {
        if (!alwaysOn && visibility.detachedSlider) {
            //resume previous visibility
            visibility = ControllerVisibility.Invisible
        }
    }
}
