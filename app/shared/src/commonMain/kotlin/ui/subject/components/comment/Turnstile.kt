/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.flow.Flow

@Stable
interface TurnstileState {
    val siteKey: String
    
    val tokenFlow: Flow<String>
    
    // reload turnstile
    fun reload()
}

@Composable
fun Turnstile(
    state: TurnstileState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        ActualTurnstile(state, constraints, Modifier)
    }
}

expect fun createTurnstileState(siteKey: String): TurnstileState

fun TurnstileState(siteKey: String): TurnstileState {
    return createTurnstileState(siteKey)
}

@Composable
expect fun ActualTurnstile(
    state: TurnstileState,
    constraints: Constraints,
    modifier: Modifier = Modifier,
)