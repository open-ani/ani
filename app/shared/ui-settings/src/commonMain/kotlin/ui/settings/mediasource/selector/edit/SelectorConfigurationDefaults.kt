/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import me.him188.ani.app.ui.foundation.effects.moveFocusOnEnter
import me.him188.ani.app.ui.foundation.layout.cardVerticalPadding

object SelectorConfigurationDefaults {
    const val STEP_NAME_1 = "步骤 1：搜索条目"
    const val STEP_NAME_2 = "步骤 2：搜索剧集"
    const val STEP_NAME_3 = "步骤 3：匹配视频"

    val verticalSpacing: Dp
        @Composable
        get() = currentWindowAdaptiveInfo().windowSizeClass.cardVerticalPadding

    val textFieldShape
        @Composable
        get() = MaterialTheme.shapes.medium
}

@Suppress("UnusedReceiverParameter")
@Composable
internal fun SelectorConfigurationDefaults.MatchVideoSection(
    state: SelectorConfigState,
    modifier: Modifier = Modifier,
    textFieldShape: Shape = SelectorConfigurationDefaults.textFieldShape,
    verticalSpacing: Dp = SelectorConfigurationDefaults.verticalSpacing,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
        val matchVideoConfig = state.matchVideoConfig
        OutlinedTextField(
            matchVideoConfig.matchVideoUrl, { matchVideoConfig.matchVideoUrl = it },
            Modifier.fillMaxWidth().moveFocusOnEnter(),
            label = { Text("匹配视频链接") },
            supportingText = { Text("从播放页面中加载的所有资源链接中匹配出视频链接的正则表达式。若正则包含名为 v 的分组则使用该分组，否则使用整个 URL") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = textFieldShape,
            isError = matchVideoConfig.matchVideoUrlIsError,
        )
    }
}
