package me.him188.ani.app.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

@Stable
interface MonoTasker {
    var isRunning: Boolean

    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    )

    fun cancel(cause: CancellationException? = null)
}

fun MonoTasker(
    scope: CoroutineScope
): MonoTasker = object : MonoTasker {
    var job: Job? = null
    override var isRunning: Boolean by mutableStateOf(false)

    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ) {
        job?.cancel()
        job = scope.launch(context, start, block)
        isRunning = true
    }

    override fun cancel(cause: CancellationException?) {
        job?.cancel(cause)
        isRunning = false
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