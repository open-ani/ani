/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.ui.foundation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.RememberObserver
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import me.him188.ani.app.session.AuthorizationCanceledException
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.trace
import moe.tlaster.precompose.viewmodel.ViewModel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

/**
 * 带有 [backgroundScope], 当 [AbstractViewModel] 被 forget 时自动 close scope 以防资源泄露.
 *
 * 因此 [AbstractViewModel] 需要与 compose remember 一起使用, 否则需手动管理生命周期.
 * 在构造 [AbstractViewModel] 时需要考虑其声明周期问题.
 */ // We can't use Android's Viewmodel because it's not available in Desktop platforms. 
abstract class AbstractViewModel : RememberObserver, ViewModel() {
    val logger by lazy { logger(this::class) }

    private val closed = atomic(false)
    private val isClosed get() = closed.value

    private var _backgroundScope = createBackgroundScope()
    val backgroundScope: CoroutineScope
        get() {
            return _backgroundScope
        }


    final override fun onAbandoned() {
        logger.trace { "${this::class.simpleName} onAbandoned" }
        dispose()
    }

    fun dispose() {
        if (!closed.compareAndSet(expect = false, update = true)) {
            return
        }
//        if (_backgroundScope.isInitialized()) {
        backgroundScope.cancel()
//        }
    }

    private var referenceCount = 0

    final override fun onForgotten() {
        referenceCount--
        logger.trace { "${this::class.simpleName} onForgotten, remaining refCount=$referenceCount" }
        if (referenceCount == 0) {
            dispose()
        }
    }

    final override fun onRemembered() {
        referenceCount++
        logger.trace { "${this::class.simpleName} onRemembered, refCount=$referenceCount" }
        if (!_backgroundScope.isActive) {
            _backgroundScope = createBackgroundScope()
        }
        if (referenceCount == 1) {
            this.init() // first remember
        }
    }

    private fun createBackgroundScope(): CoroutineScope {
        return CoroutineScope(CoroutineExceptionHandler { coroutineContext, throwable ->
            if (throwable is AuthorizationCanceledException) {
                logger.debug { "Authorization canceled" }
            } else {
                logger.error(throwable) { "Unhandled exception in background scope" }
            }
        })
    }

    /**
     * Called when the view model is remembered.
     */
    protected open fun init() {
    }

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

    inline fun <T> CoroutineScope.load(
        uuid: Uuid,
        crossinline calc: suspend () -> T?
    ): LoadingUuidItem<T> {
        val flow = MutableStateFlow<T?>(null)
        launch {
            flow.value = calc()
        }
        return LoadingUuidItem(uuid, flow)
    }

    inline fun <T, R> Flow<T>.mapLatestSupervised(crossinline transform: suspend CoroutineScope.(value: T) -> R): Flow<R> =
        mapLatest {
            supervisorScope { transform(it) }
        }

    inline fun <T> List<Uuid>.mapLoadIn(
        scope: CoroutineScope,
        crossinline calc: suspend (Uuid) -> T?,
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


fun <V : AbstractViewModel> V.launchInBackground(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend V.() -> Unit,
): Job {
    return backgroundScope.launch(context, start) {
        block()
    }
}

fun <V : AbstractViewModel> V.launchInBackgroundAnimated(
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
