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
    private var _visibility: ControllerVisibility by mutableStateOf(initialVisibility)
    val visibility get() = _visibility
    val setVisibility: (ControllerVisibility) -> Unit = {
        previousVisibility = _visibility
        _visibility = it
    }

    fun toggleVisibility(desired: ControllerVisibility? = null) {
        setVisibility(
            desired
                ?: if (_visibility == ControllerVisibility.Visible) ControllerVisibility.Invisible else ControllerVisibility.Visible,
        )
    }

    var danmakuEnabled by mutableStateOf(true)
    fun toggleDanmakuEnabled() {
        danmakuEnabled = !danmakuEnabled
    }

    private val alwaysOnRequests = SnapshotStateList<Any>()

    private var previousVisibility by mutableStateOf(ControllerVisibility.Visible) 
    /**
     * 总是显示. 也就是不要在 5 秒后自动隐藏.
     */
    val alwaysOn: Boolean by derivedStateOf {
        alwaysOnRequests.isNotEmpty()
    }
    val progressBarVisible: Boolean by derivedStateOf {
        //由于隐藏动画的存在，不能在隐藏动画发生时切换progressBar,所以隐藏动画发生时，使用previousVisibility保持当前显示的组件直至动画结束
        (_visibility.takeIf { it.bottomBar } ?: previousVisibility).detachedSlider
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
        previousVisibility = _visibility

        if (previousVisibility == ControllerVisibility.Invisible) {
            _visibility = ControllerVisibility.DetachedSliderOnly
        }
    }

    fun cancelRequestProgressBarVisible() {
        if (!alwaysOn) {
            //resume previous visibility
            val currentVisibility = _visibility
            _visibility = previousVisibility
            previousVisibility = currentVisibility
        }
    }
}
