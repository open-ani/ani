package me.him188.ani.app.ui.framework

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlin.time.Duration.Companion.minutes

/**
 * 相对于 [runComposeUiTest], 有一些修改:
 * - [ComposeUiTest.waitUntil] 的超时时间更长
 */
@OptIn(DelicateCoroutinesApi::class)
actual fun runAniComposeUiTest(testBody: AniComposeUiTest.() -> Unit) {
    // 一定要调用这个, 否则在跟其他协程测试一起跑的时候, lifecycle 的一个地方会一直 block
    // androidx.lifecycle.MainDispatcherChecker.updateMainDispatcherThread
    Dispatchers.resetMain()


    val testThread = Thread.currentThread()
    var timedOut = false
    val job = GlobalScope.launch {
        delay(1.minutes)
        timedOut = true
        testThread.interrupt()
    }

    try {
        return runComposeUiTest {
            AniComposeUiTestImpl(this).run(testBody)
        }
    } catch (e: InterruptedException) {
        if (timedOut) {
            throw AssertionError("Test timed out after 1 minute")
        } else {
            throw e
        }
    } finally {
        job.cancel()
    }
}

internal class AniComposeUiTestImpl(composeUiTest: ComposeUiTest) : AbstractAniComposeUiTest(composeUiTest) {
    override fun waitUntil(conditionDescription: String?, timeoutMillis: Long, condition: () -> Boolean) =
        composeUiTest.waitUntil(conditionDescription, timeoutMillis, condition)
}
