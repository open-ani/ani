package me.him188.ani.app.videoplayer.ui.guesture

import androidx.annotation.MainThread
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import me.him188.ani.app.platform.AudioManager
import me.him188.ani.app.platform.StreamType

interface LevelController {
    @MainThread
    fun increaseLevel()

    @MainThread
    fun decreaseLevel()
}

fun AudioManager.asLevelController(
    streamType: StreamType,
): LevelController = object : LevelController {
    override fun increaseLevel() {
        val current = getVolume(streamType)
        setVolume(streamType, (current + 0.05f).coerceAtMost(1f))
    }

    override fun decreaseLevel() {
        val current = getVolume(streamType)
        setVolume(streamType, (current - 0.05f).coerceAtLeast(0f))
    }
}

fun Modifier.swipeLevelControl(
    controller: LevelController,
    stepSize: Dp,
    orientation: Orientation,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "swipeLevelControl"
        properties["controller"] = controller
        properties["stepSize"] = stepSize
        properties["orientation"] = orientation
    }
) {
    steppedDraggable(
        rememberSteppedDraggableState(
            stepSize = stepSize,
            onStep = { direction ->
                when (direction) {
                    StepDirection.FORWARD -> controller.increaseLevel()
                    StepDirection.BACKWARD -> controller.decreaseLevel()
                }
            },
        ),
        orientation = orientation,
    )

}