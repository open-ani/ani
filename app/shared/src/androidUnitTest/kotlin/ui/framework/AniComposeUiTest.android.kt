package me.him188.ani.app.ui.framework

/**
 * 相对于 [runComposeUiTest], 有一些修改:
 * - [ComposeUiTest.waitUntil] 的超时时间更长
 */
actual fun runAniComposeUiTest(testBody: AniComposeUiTest.() -> Unit) {
    // TODO: Compose Multiplatform UI test is currently (1.7.0) not supported on Android
}
