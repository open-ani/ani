/*
 * Copyright 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.navigation

import androidx.lifecycle.LifecycleOwner
import me.him188.ani.app.navigation.AniNavigator

class SkikoOnBackPressedDispatcherOwner(
    private val aniNavigator: AniNavigator, lifecycleOwner: LifecycleOwner,
) : OnBackPressedDispatcherOwner, LifecycleOwner by lifecycleOwner {
    override val onBackPressedDispatcher: OnBackPressedDispatcher = OnBackPressedDispatcher(
        fallback = {
            aniNavigator.popBackStack()
        },
    )
}
