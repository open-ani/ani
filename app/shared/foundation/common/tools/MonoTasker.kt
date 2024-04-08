package me.him188.ani.app.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface MonoTasker {
    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    )
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
}

@Composable
fun rememberMonoTasker(): MonoTasker {
    val uiScope = rememberCoroutineScope()
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
        }
    }
    return tasker
}