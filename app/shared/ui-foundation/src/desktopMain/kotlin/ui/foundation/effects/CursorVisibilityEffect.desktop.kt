package me.him188.ani.app.ui.foundation.effects

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.testTag
import me.him188.ani.app.platform.window.AwtWindowUtils.Companion.blankCursor

const val TAG_CURSOR_VISIBILITY_EFFECT_VISIBLE = "CursorVisibilityEffect-visible"
const val TAG_CURSOR_VISIBILITY_EFFECT_INVISIBLE = "CursorVisibilityEffect-invisible"

actual fun Modifier.cursorVisibility(visible: Boolean): Modifier {
    return if (visible) {
        testTag(TAG_CURSOR_VISIBILITY_EFFECT_VISIBLE)
    } else {
        pointerHoverIcon(PointerIcon(blankCursor)).testTag(TAG_CURSOR_VISIBILITY_EFFECT_INVISIBLE)
    }
}
