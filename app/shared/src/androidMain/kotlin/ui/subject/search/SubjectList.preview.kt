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

package me.him188.ani.app.ui.subject.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.home.search.SubjectPreviewColumn
import me.him188.ani.app.ui.home.search.SubjectPreviewListState
import me.him188.ani.app.ui.subject.details.components.TestSubjectInfo
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
