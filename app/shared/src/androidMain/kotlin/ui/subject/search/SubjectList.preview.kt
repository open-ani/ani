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
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.subject.RatingInfo
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.home.search.SubjectListViewModel
import me.him188.ani.app.ui.home.search.SubjectPreviewColumn
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged

@Composable
@Preview(apiLevel = 33)
private fun PreviewSubjectList() {
    val viewModel = remember {
        SubjectListViewModel(
            PageBasedPagedSource {
                Paged(
                    listOf(
                        SubjectInfo.Empty.copy(
                            id = 400602,
                            name = "葬送的芙莉莲",
                            nameCn = "葬送的芙莉莲",
                            tags = listOf(),
                            summary = "",
                            ratingInfo = RatingInfo.Empty,
                        ),
                    ),
                )
            },
        )
    }
    ProvideCompositionLocalsForPreview {
        SubjectPreviewColumn(viewModel)
    }
}