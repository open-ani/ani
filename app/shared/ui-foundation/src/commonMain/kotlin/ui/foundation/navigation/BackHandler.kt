/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.lifecycle.LifecycleOwner
import me.him188.ani.utils.platform.annotations.TestOnly

/**
 * Android port 到官方, skiko 自己实现
 */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)

// 对应安卓的结构
expect object LocalOnBackPressedDispatcherOwner {
    val current: OnBackPressedDispatcherOwner?
        @Composable get

    infix fun provides(dispatcherOwner: OnBackPressedDispatcherOwner):
            ProvidedValue<OnBackPressedDispatcherOwner?>
}

expect interface OnBackPressedDispatcherOwner : LifecycleOwner {
    val onBackPressedDispatcher: OnBackPressedDispatcher
}

expect class OnBackPressedDispatcher {
    fun onBackPressed()
}

@TestOnly
expect fun OnBackPressedDispatcher(
    fallbackOnBackPressed: (() -> Unit)? = null
): OnBackPressedDispatcher 
