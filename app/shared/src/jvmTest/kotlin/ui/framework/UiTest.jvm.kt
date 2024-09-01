package me.him188.ani.app.ui.framework

import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

/**
 * 相对于 [runComposeUiTest], 有一些修改:
 * - [ComposeUiTest.waitUntil] 的超时时间更长
 */
@OptIn(DelicateCoroutinesApi::class)
actual fun runAniComposeUiTest(testBody: AniComposeUiTest.() -> Unit) = runComposeUiTest {
    val testThread = Thread.currentThread()
    var timedOut = false
    val job = GlobalScope.launch {
        delay(1.minutes)
        timedOut = true
        testThread.interrupt()
    }

    try {
        AniComposeUiTestImpl(this).run(testBody)
    } catch (e: InterruptedException) {
        if (timedOut) {
            throw AssertionError("Test timed out after 1 minute")
        } else {
            throw e
        }
    }

    job.cancel()
}
