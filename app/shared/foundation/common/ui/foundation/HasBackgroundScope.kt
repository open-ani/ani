package me.him188.ani.app.ui.foundation

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

interface HasBackgroundScope {
    val backgroundScope: CoroutineScope

    fun <T> Flow<T>.shareInBackground(
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
        replay: Int = 1,
    ): SharedFlow<T> = shareIn(backgroundScope, started, replay)

    fun <T> Flow<T>.stateInBackground(
        initialValue: T,
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
    ): StateFlow<T> = stateIn(backgroundScope, started, initialValue)

    fun <T> Flow<T>.stateInBackground(
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
    ): StateFlow<T?> = stateIn(backgroundScope, started, null)


    fun <T> Flow<T>.runningList(): Flow<List<T>> {
        return runningFold(emptyList()) { acc, value ->
            acc + value
        }
    }

    fun <T> deferFlowInBackground(value: suspend () -> T): MutableStateFlow<T?> {
        val flow = MutableStateFlow<T?>(null)
        launchInBackground {
            flow.value = value()
        }
        return flow
    }

    fun <T> CoroutineScope.load(
        uuid: Uuid,
        calc: suspend () -> T?
    ): LoadingUuidItem<T> {
        val flow = MutableStateFlow<T?>(null)
        launch {
            flow.value = calc()
        }
        return LoadingUuidItem(uuid, flow)
    }

    fun <T, R> Flow<T>.mapLatestSupervised(transform: suspend CoroutineScope.(value: T) -> R): Flow<R> =
        mapLatest {
            supervisorScope { transform(it) }
        }

    fun <T> List<Uuid>.mapLoadIn(
        scope: CoroutineScope,
        calc: suspend (Uuid) -> T?,
    ): List<LoadingUuidItem<T>> {
        return map { scope.load(it) { calc(it) } }
    }

    fun <T> Flow<T>.localCachedStateFlow(initialValue: T): MutableStateFlow<T> {
        val localFlow = MutableStateFlow(initialValue)
        val mergedFlow: StateFlow<T> = merge(this, localFlow).stateInBackground(initialValue)
        return object : MutableStateFlow<T> by localFlow {
            override var value: T
                get() = mergedFlow.value
                set(value) {
                    localFlow.value = value
                }

            override val replayCache: List<T> get() = mergedFlow.replayCache

            override suspend fun collect(collector: FlowCollector<T>): Nothing {
                mergedFlow.collect(collector)
            }
        }
    }

    fun <T> Flow<T>.localCachedSharedFlow(
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
        replay: Int = 1,
        onBufferOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST,
    ): MutableSharedFlow<T> {
        val localFlow = MutableSharedFlow<T>(replay, onBufferOverflow = onBufferOverflow)
        val mergedFlow: SharedFlow<T> = merge(this, localFlow).shareInBackground(started, replay = replay)
        return object : MutableSharedFlow<T> by localFlow {
            override val replayCache: List<T> get() = mergedFlow.replayCache

            override suspend fun collect(collector: FlowCollector<T>): Nothing {
                mergedFlow.collect(collector)
            }
        }
    }
}


fun <V : HasBackgroundScope> V.launchInBackground(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend V.() -> Unit,
): Job {
    return backgroundScope.launch(context, start) {
        block()
    }
}

fun <V : HasBackgroundScope> V.launchInMain(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend V.() -> Unit,
): Job {
    return backgroundScope.launch(context + Dispatchers.Main.immediate, start) {
        block()
    }
}

fun <V : HasBackgroundScope> V.launchInBackgroundAnimated(
    isLoadingState: MutableState<Boolean>,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend V.() -> Unit,
): Job {
    isLoadingState.value = true
    return backgroundScope.launch(context, start) {
        block()
        isLoadingState.value = false
    }
}


fun <T> CoroutineScope.deferFlow(value: suspend () -> T): MutableStateFlow<T?> {
    val flow = MutableStateFlow<T?>(null)
    launch {
        flow.value = value()
    }
    return flow
}
