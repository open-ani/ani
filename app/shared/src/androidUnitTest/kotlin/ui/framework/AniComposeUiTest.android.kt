package me.him188.ani.app.ui.framework

import androidx.compose.ui.graphics.ImageBitmap

/**
 * 相对于 [runComposeUiTest], 有一些修改:
 * - [ComposeUiTest.waitUntil] 的超时时间更长
 */
actual fun runAniComposeUiTest(testBody: AniComposeUiTest.() -> Unit) {
    // TODO: Compose Multiplatform UI test is currently (1.7.0) not supported on Android
}

actual fun ImageBitmap.assertScreenshot(expectedResource: String) {
    // TODO: Compose Multiplatform UI test is currently (1.7.0) not supported on Android
}

/**
 * 截图当前的 UI 并与 resources 目录下的图片 [expectedResource] 进行比较.
 */
actual fun AniComposeUiTest.assertScreenshot(expectedResource: String) {
    // TODO: Compose Multiplatform UI test is currently (1.7.0) not supported on Android
}
