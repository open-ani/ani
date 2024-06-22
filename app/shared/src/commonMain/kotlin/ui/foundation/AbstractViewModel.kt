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

import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import me.him188.ani.app.session.AuthorizationCancelledException
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.trace
import moe.tlaster.precompose.stateholder.LocalSavedStateHolder
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.SavedStateHolder
import moe.tlaster.precompose.stateholder.StateHolder
import moe.tlaster.precompose.viewmodel.ViewModel
import kotlin.reflect.KClass

/**
 * 带有 [backgroundScope], 当 [AbstractViewModel] 被 forget 时自动 close scope 以防资源泄露.
 *
 * 因此 [AbstractViewModel] 需要与 compose remember 一起使用, 否则需手动管理生命周期.
 * 在构造 [AbstractViewModel] 时需要考虑其声明周期问题.
 */ // We can't use Android's Viewmodel because it's not available in Desktop platforms. 
abstract class AbstractViewModel : RememberObserver, ViewModel(), HasBackgroundScope {
    val logger by lazy { logger(this::class) }

    private var _backgroundScope = createBackgroundScope()
    override val backgroundScope: CoroutineScope
        get() {
            return _backgroundScope
        }


    private var referenceCount = 0

    @CallSuper
    override fun onAbandoned() {
        referenceCount--
    }

    @CallSuper
    override fun onForgotten() {
        referenceCount--
    }

    @CallSuper
    override fun onRemembered() {
        referenceCount++
        logger.trace { "${this::class.simpleName} onRemembered, refCount=$referenceCount" }
        if (referenceCount == 1) {
            this.init() // first remember
        }
    }

    private fun createBackgroundScope(): CoroutineScope {
        return CoroutineScope(
            CoroutineExceptionHandler { _, throwable ->
                if (throwable is AuthorizationCancelledException) {
                    logger.debug { "Authorization canceled" }
                } else {
                    logger.error(throwable) { "Unhandled exception in background scope" }
                }
            } + SupervisorJob(),
        )
    }

    /**
     * Called when the view model is remembered the first time.
     */
    protected open fun init() {
    }

    @CallSuper
    override fun close() {
        super.close()
    }

    override fun onCleared() {
        backgroundScope.cancel()
        super.onCleared()
    }
}


/**
 * Returns a [ViewModel] instance that is scoped to the given [StateHolder].
 * @param keys A list of keys that will be used to identify the ViewModel.
 * @param creator A function that will be used to create the ViewModel if it doesn't exist.
 * @return A ViewModel instance.
 */
@Composable
inline fun <reified T : Any> rememberViewModel(
    keys: List<Any?> = emptyList(),
    noinline creator: (SavedStateHolder) -> T,
): T = rememberViewModel(T::class, keys, creator = creator)

/**
 * Returns a [ViewModel] instance that is scoped to the given [StateHolder].
 * @param modelClass The class of the ViewModel.
 * @param keys A list of keys that will be used to identify the ViewModel.
 * @param creator A function that will be used to create the ViewModel if it doesn't exist.
 * @return A ViewModel instance.
 */
@Composable
fun <T : Any> rememberViewModel(
    modelClass: KClass<T>,
    keys: List<Any?> = emptyList(),
    creator: (SavedStateHolder) -> T,
): T {
    val stateHolder = checkNotNull(LocalStateHolder.current) {
        "Require LocalStateHolder not null for $modelClass"
    }
    val savedStateHolder = checkNotNull(LocalSavedStateHolder.current) {
        "Require LocalSavedStateHolder not null"
    }
    return remember(
        modelClass,
        keys,
        creator,
        stateHolder,
        savedStateHolder,
    ) {
        stateHolder.getViewModel(keys, modelClass = modelClass) {
            creator(savedStateHolder)
        }
    }
}

private fun <T : Any> StateHolder.getViewModel(
    keys: List<Any?> = emptyList(),
    modelClass: KClass<T>,
    creator: () -> T,
): T {
    val key = (keys.map { it.hashCode().toString() } + modelClass.simpleName).joinToString()
    return this.getOrPut(key) {
        creator()
    }
}
