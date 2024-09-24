/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

@file:OptIn(TestOnly::class)

package me.him188.ani.app.ui.settings.mediasource.selector.edit

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.source.media.source.web.SelectorMediaSourceArguments
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormatFlattened
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormatNoChannel
import me.him188.ani.app.data.source.media.source.web.format.SelectorFormatId
import me.him188.ani.app.ui.foundation.ProvideFoundationCompositionLocalsForPreview
import me.him188.ani.app.ui.settings.mediasource.rss.createTestSaveableStorage
import me.him188.ani.utils.platform.annotations.TestOnly

@Composable
@TestOnly
fun rememberTestSelectorConfigurationState(
    arguments: SelectorMediaSourceArguments = SelectorMediaSourceArguments.Default
): SelectorConfigState {
    return remember {
        SelectorConfigState(
            createTestSaveableStorage(
                arguments,
            ),
        )
    }
}

@Composable
@Preview
private fun PreviewSelectorChannelConfigurationColumnNotFound() = ProvideFoundationCompositionLocalsForPreview {
    Surface {
        SelectorChannelFormatColumn(SelectorFormatId("dummy"), rememberTestSelectorConfigurationState())
    }
}

@Composable
@Preview
private fun PreviewSelectorChannelConfigurationColumnFlattened() = ProvideFoundationCompositionLocalsForPreview {
    Surface {
        SelectorChannelFormatColumn(SelectorChannelFormatFlattened.id, rememberTestSelectorConfigurationState())
    }
}

@Composable
@Preview
private fun PreviewSelectorChannelConfigurationColumnNoChannel() = ProvideFoundationCompositionLocalsForPreview {
    Surface {
        SelectorChannelFormatColumn(SelectorChannelFormatNoChannel.id, rememberTestSelectorConfigurationState())
    }
}
