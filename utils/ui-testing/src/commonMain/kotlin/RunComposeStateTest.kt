package me.him188.ani.app.ui.framework

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


/**
 * [Dispatchers.Main] 设置为 test dispatcher, 让一些 state 操作与测试代码在同一调度队列执行. (例如 `HasBackgroundScope.produceState` 会在 [Dispatchers.Main] 执行)
 *
 * 可以使用 [TestCoroutineScheduler.runCurrent] 来执行所有后台任务.
 * 使用 [takeSnapshot] 来让所有 Compose snapshot state [androidx.compose.runtime.State] 更新, 例如 [derivedStateOf]
 */
fun runComposeStateTest(
    context: CoroutineContext = EmptyCoroutineContext,
    testBody: suspend TestScope.() -> Unit
) = runTest(context) {
    setDispatcher()
    try {
        testBody()
    } finally {
        Dispatchers.resetMain()
    }
}

fun runComposeStateTest(
    context: CoroutineContext = EmptyCoroutineContext,
    timeout: Duration = 60.seconds,
    testBody: suspend TestScope.() -> Unit
) = runTest(context, timeout) {
    setDispatcher()
    try {
        testBody()
    } finally {
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalStdlibApi::class)
private suspend inline fun setDispatcher() {
    Dispatchers.setMain(currentCoroutineContext()[CoroutineDispatcher]!!)
}

suspend fun TestScope.takeSnapshot() {
    // magic
    yield()
    testScheduler.runCurrent()
    Snapshot.sendApplyNotifications()
}
