package me.him188.ani.app.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

interface MonoTasker {
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
    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ) {
        job?.cancel()
        job = scope.launch(context, start, block)
    }

    override fun cancel(cause: CancellationException?) {
        job?.cancel(cause)
    }
}

// ui (composition) scope
@Composable
inline fun rememberMonoTasker(
    crossinline getContext: @DisallowComposableCalls () -> CoroutineContext = { EmptyCoroutineContext }
): MonoTasker {
    val uiScope = rememberCoroutineScope(getContext)
    val tasker = remember {
        object : MonoTasker {
            var job: Job? = null
            override fun launch(
                context: CoroutineContext,
                start: CoroutineStart,
                block: suspend CoroutineScope.() -> Unit
            ) {
                job?.cancel()
                job = uiScope.launch(context, start, block)
            }

            override fun cancel(cause: CancellationException?) {
                job?.cancel(cause)
            }
        }
    }
    return tasker
}