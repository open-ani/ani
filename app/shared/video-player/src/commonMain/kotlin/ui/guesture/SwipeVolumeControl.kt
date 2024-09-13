package me.him188.ani.app.videoplayer.ui.guesture

import androidx.annotation.MainThread
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import me.him188.ani.app.videoplayer.freatures.AudioManager
import me.him188.ani.app.videoplayer.freatures.BrightnessManager
import me.him188.ani.app.videoplayer.freatures.StreamType

interface LevelController {
    val level: Float

    @MainThread
    fun increaseLevel(step: Float = 0.05f)

    @MainThread
    fun decreaseLevel(step: Float = 0.05f)
}

fun AudioManager.asLevelController(
    streamType: StreamType,
): LevelController = object : LevelController {
    override val level: Float
        get() = getVolume(streamType)

    override fun increaseLevel(step: Float) {
        val current = getVolume(streamType)
        setVolume(streamType, (current + step).coerceAtMost(1f))
    }

    override fun decreaseLevel(step: Float) {
        val current = getVolume(streamType)
        setVolume(streamType, (current - step).coerceAtLeast(0f))
    }
}

fun BrightnessManager.asLevelController(): LevelController = object : LevelController {
    override val level: Float
        get() = getBrightness()

    override fun increaseLevel(step: Float) {
        val current = getBrightness()
        setBrightness((current + step).coerceAtMost(1f))
    }

    override fun decreaseLevel(step: Float) {
        val current = getBrightness()
        setBrightness((current - step).coerceAtLeast(0f))
    }
}

fun Modifier.swipeLevelControl(
    controller: LevelController,
    stepSize: Dp,
    orientation: Orientation,
    step: Float = 0.05f,
    afterStep: (StepDirection) -> Unit = {},
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "swipeLevelControl"
        properties["controller"] = controller
        properties["stepSize"] = stepSize
        properties["orientation"] = orientation
    },
) {
    steppedDraggable(
        rememberSteppedDraggableState(
            stepSize = stepSize,
            onStep = { direction ->
                when (direction) {
                    StepDirection.FORWARD -> controller.increaseLevel(step)
                    StepDirection.BACKWARD -> controller.decreaseLevel(step)
                }
                afterStep(direction)
            },
        ),
        orientation = orientation,
    )

}