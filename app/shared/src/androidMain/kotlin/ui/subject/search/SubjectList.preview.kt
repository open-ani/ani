/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import me.him188.ani.app.ui.exploration.search.SubjectPreviewColumn
import me.him188.ani.app.ui.exploration.search.SubjectPreviewListState
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.subject.details.TestSubjectInfo
import me.him188.ani.utils.platform.annotations.TestOnly

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
@PreviewFontScale
private fun PreviewSubjectList() {
    ProvideCompositionLocalsForPreview {
        SubjectPreviewColumn(
            rememberTestSubjectPreviewListState(),
        )
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
@TestOnly
fun rememberTestSubjectPreviewListState() = remember {
    SubjectPreviewListState(
        stateOf(listOf(TestSubjectInfo)),
        stateOf(false),
        {},
        GlobalScope,
    )
}
