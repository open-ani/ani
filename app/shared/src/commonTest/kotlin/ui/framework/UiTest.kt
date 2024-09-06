package me.him188.ani.app.ui.framework

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.IdlingResource
import androidx.compose.ui.test.MainTestClock
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * [Dispatchers.Main] 设置为 test dispatcher, 让一些 state 操作与测试代码在同一调度队列执行. (例如 [HasBackgroundScope.produceState] 会在 [Dispatchers.Main] 执行)
 *
 * 可以使用 [TestCoroutineScheduler.runCurrent] 来执行所有后台任务.
 * 使用 [takeSnapshot] 来让所有 Compose snapshot state [androidx.compose.runtime.State] 更新, 例如 [derivedStateOf]
 */
fun runComposeStateTest(
    context: CoroutineContext = EmptyCoroutineContext,
    testBody: suspend TestScope.() -> Unit
) = runTest(context) {
    setDispatcher()
    testBody()
}

fun runComposeStateTest(
    context: CoroutineContext = EmptyCoroutineContext,
    timeout: Duration = 60.seconds,
    testBody: suspend TestScope.() -> Unit
) = runTest(context, timeout) {
    setDispatcher()
    testBody()
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

///////////////////////////////////////////////////////////////////////////
// runAniComposeUiTest
///////////////////////////////////////////////////////////////////////////

/**
 * 相对于 [runComposeUiTest], 有一些修改:
 * - [ComposeUiTest.waitUntil] 的超时时间更长
 */
expect fun runAniComposeUiTest(
    testBody: AniComposeUiTest.() -> Unit
)

internal class AniComposeUiTestImpl(override val composeUiTest: ComposeUiTest) : AniComposeUiTest {
    override val density: Density
        get() = composeUiTest.density
    override val mainClock: MainTestClock
        get() = composeUiTest.mainClock

    override fun <T> runOnUiThread(action: () -> T): T = composeUiTest.runOnUiThread(action)
    override fun <T> runOnIdle(action: () -> T): T = composeUiTest.runOnIdle(action)
    override fun waitForIdle() = composeUiTest.waitForIdle()
    override suspend fun awaitIdle() = composeUiTest.awaitIdle()

    override fun waitUntil(conditionDescription: String?, timeoutMillis: Long, condition: () -> Boolean) =
        composeUiTest.waitUntil(conditionDescription, timeoutMillis, condition)

    override fun registerIdlingResource(idlingResource: IdlingResource) =
        composeUiTest.registerIdlingResource(idlingResource)

    override fun unregisterIdlingResource(idlingResource: IdlingResource) =
        composeUiTest.unregisterIdlingResource(idlingResource)

    override fun setContent(composable: @Composable () -> Unit) = composeUiTest.setContent(composable)

    override fun onAllNodes(matcher: SemanticsMatcher, useUnmergedTree: Boolean): SemanticsNodeInteractionCollection =
        composeUiTest.onAllNodes(matcher, useUnmergedTree)

    override fun onNode(matcher: SemanticsMatcher, useUnmergedTree: Boolean): SemanticsNodeInteraction =
        composeUiTest.onNode(matcher, useUnmergedTree)
}

// body copied from ComposeUiTest
/**
 * @see ComposeUiTest
 */
interface AniComposeUiTest : SemanticsNodeInteractionsProvider {
    val composeUiTest: ComposeUiTest

    val density: Density
    val mainClock: MainTestClock
    fun <T> runOnUiThread(action: () -> T): T
    fun <T> runOnIdle(action: () -> T): T
    fun waitForIdle()
    suspend fun awaitIdle()


    /**
     * Blocks until the given [condition] is satisfied.
     *
     * If [auto advancement][MainTestClock.autoAdvance] is enabled on the [mainClock], this method
     * will actively advance the clock to process any pending composition, invalidation and
     * animation. If auto advancement is not enabled, the clock will not be advanced actively
     * which usually means that the Compose UI appears to be frozen. This is ideal for testing
     * animations in a deterministic way. In either case, this method will wait for all
     * [IdlingResource]s to become idle.
     *
     * Note that some processes are driven by the host operating system and will therefore still
     * execute when auto advancement is disabled. For example, on Android measure, layout and draw
     * can still happen if the host view is invalidated by other parts of the View hierarchy.
     *
     * Compared to [MainTestClock.advanceTimeUntil], [waitUntil] sleeps after every iteration to
     * give the host operating system the opportunity to do measure/layout/draw passes. This gives
     * [waitUntil] a better integration with the host, but it is less preferred from a performance
     * viewpoint. Therefore, we recommend that you try using [MainTestClock.advanceTimeUntil]
     * before resorting to [waitUntil].
     *
     * @param timeoutMillis The time after which this method throws an exception if the given
     * condition is not satisfied. This observes wall clock time, not [frame time][mainClock].
     * @param conditionDescription An optional human-readable description of [condition] that will
     * be included in the timeout exception if thrown.
     * @param condition Condition that must be satisfied in order for this method to successfully
     * finish.
     *
     * @throws androidx.compose.ui.test.ComposeTimeoutException If the condition is not satisfied
     * after [timeoutMillis] (in wall clock time).
     */
    fun waitUntil(
        conditionDescription: String? = null,
        timeoutMillis: Long = 5000L,
        condition: () -> Boolean
    )

    // 默认 timeoutMillis 更长, 否则 CI 上有概率失败
    fun waitUntil(timeoutMillis: Long = 5000L, condition: () -> Boolean) {
        waitUntil(null, timeoutMillis, condition)
    }

    fun registerIdlingResource(idlingResource: IdlingResource)
    fun unregisterIdlingResource(idlingResource: IdlingResource)
    fun setContent(composable: @Composable () -> Unit)
}