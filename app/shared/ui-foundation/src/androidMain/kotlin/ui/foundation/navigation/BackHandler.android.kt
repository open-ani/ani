/*
 * Copyright 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

@file:Suppress("ACTUAL_CLASSIFIER_MUST_HAVE_THE_SAME_MEMBERS_AS_NON_FINAL_EXPECT_CLASSIFIER_WARNING")

package me.him188.ani.app.ui.foundation.navigation

import androidx.compose.runtime.Composable
import me.him188.ani.utils.platform.annotations.TestOnly

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) = androidx.activity.compose.BackHandler(enabled, onBack)

actual typealias LocalOnBackPressedDispatcherOwner = androidx.activity.compose.LocalOnBackPressedDispatcherOwner

actual typealias OnBackPressedDispatcherOwner = androidx.activity.OnBackPressedDispatcherOwner

actual typealias OnBackPressedDispatcher = androidx.activity.OnBackPressedDispatcher

@TestOnly
actual fun OnBackPressedDispatcher(fallbackOnBackPressed: (() -> Unit)?): OnBackPressedDispatcher {
    return androidx.activity.OnBackPressedDispatcher(fallbackOnBackPressed)
}
