package me.him188.ani.app.ui.framework

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.runComposeUiTest

/**
 * 相对于 [runComposeUiTest], 有一些修改:
 * - [ComposeUiTest.waitUntil] 的超时时间更长
 */
actual fun runAniComposeUiTest(testBody: AniComposeUiTest.() -> Unit) = runComposeUiTest {
    AniComposeUiTestImpl(this).run(testBody)
}
