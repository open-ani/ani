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
    state: SelectorConfigurationState,
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
            supportingText = { Text("从播放页面中加载的所有资源链接中匹配出视频链接的正则表达式。将会使用匹配结果的分组 v") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = textFieldShape,
            isError = matchVideoConfig.matchVideoUrlIsError,
        )

        val conf = matchVideoConfig.videoHeaders
        OutlinedTextField(
            conf.referer, { conf.referer = it },
            Modifier.fillMaxWidth().moveFocusOnEnter(),
            label = { Text("Referer") },
            supportingText = { Text("HTTP 请求的 Referer") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = textFieldShape,
        )
        OutlinedTextField(
            conf.userAgent, { conf.userAgent = it },
            Modifier.fillMaxWidth().moveFocusOnEnter(),
            label = { Text("User-Agent") },
            supportingText = { Text("HTTP 请求的 User-Agent") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = textFieldShape,
        )
    }
}
