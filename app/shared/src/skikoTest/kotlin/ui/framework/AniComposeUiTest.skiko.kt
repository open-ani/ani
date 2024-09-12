package me.him188.ani.app.ui.framework

import androidx.compose.ui.test.SkikoComposeUiTest

/**
 * 截图当前的 UI 并与 resources 目录下的图片 [expectedResource] 进行比较.
 */
actual fun AniComposeUiTest.assertScreenshot(expectedResource: String) {
    return (this.composeUiTest as SkikoComposeUiTest).assertScreenshot(expectedResource)
}
