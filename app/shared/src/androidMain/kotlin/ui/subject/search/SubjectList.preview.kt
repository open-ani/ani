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
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.subject.Subject
import me.him188.ani.datasources.api.subject.SubjectImages

@Composable
@Preview(apiLevel = 33)
private fun PreviewSubjectList() {
    val viewModel = remember {
        SubjectListViewModel(
            PageBasedPagedSource {
                Paged(
                    listOf(
                        Subject(
                            id = 400602,
                            originalName = "葬送的芙莉莲",
                            chineseName = "葬送的芙莉莲",
                            score = 8.0,
                            rank = 100,
                            sourceUrl = "https://bgm.tv/subject/400602",
                            images = SubjectImages(
                                "https://lain.bgm.tv/pic/cover/l/13/c5/400602_ZI8Y9.jpg?_gl=1*isepc9*_ga*NDQzNzcwOTYyLjE3MDM4NjE5NzQ.*_ga_1109JLGMHN*MTcwNDQwNjE1MS4xMC4xLjE3MDQ0MDYxNzYuMC4wLjA.",
                                "https://lain.bgm.tv/pic/cover/l/13/c5/400602_ZI8Y9.jpg?_gl=1*isepc9*_ga*NDQzNzcwOTYyLjE3MDM4NjE5NzQ.*_ga_1109JLGMHN*MTcwNDQwNjE1MS4xMC4xLjE3MDQ0MDYxNzYuMC4wLjA.",
                            ),

                            tags = listOf(),
                            summary = "",
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