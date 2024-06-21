package me.him188.ani.app.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.produceState
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

@Stable
interface MonoTasker {
    val isRunning: Boolean

    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    )

    /**
     * 等待上一个任务完成后再执行
     */
    fun launchNext(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    )

    fun cancel(cause: CancellationException? = null)

    suspend fun cancelAndJoin()

    suspend fun join()
}

fun MonoTasker(
    scope: CoroutineScope
): MonoTasker = object : MonoTasker {
    var job: Job? = null

    private val _isRunning = MutableStateFlow(false)
    override val isRunning: Boolean by _isRunning.produceState(false, scope)

    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ) {
        job?.cancel()
        job = scope.launch(context, start, block).apply {
            invokeOnCompletion {
                if (job === this) {
                    _isRunning.value = false
                }
            }
        }
        _isRunning.value = true
    }

    override fun launchNext(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val existingJob = job
        job = scope.launch(context, start) {
            try {
                existingJob?.join()
                block()
            } catch (e: CancellationException) {
                existingJob?.cancel()
                throw e
            }
        }.apply {
            invokeOnCompletion {
                if (job === this) {
                    _isRunning.value = false
                }
            }
        }
        _isRunning.value = true
    }

    override fun cancel(cause: CancellationException?) {
        job?.cancel(cause) // use completion handler to set _isRunning to false
    }

    override suspend fun cancelAndJoin() {
        job?.run {
            join()
        }
    }

    override suspend fun join() {
        job?.join()
    }
}

// ui (composition) scope
@Composable
inline fun rememberUiMonoTasker(
    crossinline getContext: @DisallowComposableCalls () -> CoroutineContext = { EmptyCoroutineContext }
): MonoTasker {
    val uiScope = rememberCoroutineScope(getContext)
    val tasker = remember(uiScope) { MonoTasker(uiScope) }
    return tasker
}

@Composable
inline fun HasBackgroundScope.rememberBackgroundMonoTasker(
    crossinline getContext: @DisallowComposableCalls () -> CoroutineContext = { EmptyCoroutineContext }
): MonoTasker {
    val tasker = remember(this) { MonoTasker(backgroundScope) }
    return tasker
}