/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.app.ui.subject

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.him188.animationgarden.datasources.api.SearchSession
import me.him188.animationgarden.datasources.api.Subject
import me.him188.animationgarden.datasources.api.SubjectImages


@Preview(apiLevel = 33)
@Composable
private fun PreviewSubjectPreviewCard() {
    Row {
        SubjectPreviewCard(
            title = "葬送的芙莉莲",
            imageUrl = "https://lain.bgm.tv/pic/cover/l/13/c5/400602_ZI8Y9.jpg?_gl=1*isepc9*_ga*NDQzNzcwOTYyLjE3MDM4NjE5NzQ.*_ga_1109JLGMHN*MTcwNDQwNjE1MS4xMC4xLjE3MDQ0MDYxNzYuMC4wLjA.",
            onClick = {

            },
            Modifier.weight(0.5f)
        )
    }
}


@Composable
@Preview(apiLevel = 33)
private fun PreviewSubjectList() {
    val viewModel = remember {
        SubjectListViewModel(object : SearchSession<Subject> {
            override val results: Flow<Subject> = flow {
                while (true) {
                    emit(subject)
                }
            }

            private val subject
                get() = Subject(
                    officialName = "葬送的芙莉莲",
                    chineseName = "葬送的芙莉莲",
                    episodeCount = 12,
                    ratingScore = 8.0,
                    ratingCount = 100,
                    rank = 1,
                    sourceUrl = "https://bgm.tv/subject/400602",
                    images = object : SubjectImages {
                        override fun landscapeCommon(): String =
                            "https://lain.bgm.tv/pic/cover/l/13/c5/400602_ZI8Y9.jpg?_gl=1*isepc9*_ga*NDQzNzcwOTYyLjE3MDM4NjE5NzQ.*_ga_1109JLGMHN*MTcwNDQwNjE1MS4xMC4xLjE3MDQ0MDYxNzYuMC4wLjA."

                        override fun largePoster(): String =
                            "https://lain.bgm.tv/pic/cover/l/13/c5/400602_ZI8Y9.jpg?_gl=1*isepc9*_ga*NDQzNzcwOTYyLjE3MDM4NjE5NzQ.*_ga_1109JLGMHN*MTcwNDQwNjE1MS4xMC4xLjE3MDQ0MDYxNzYuMC4wLjA."
                    }
                )

            private val maxCount = 15
            private var count = 0
            override suspend fun nextPageOrNull(): List<Subject>? {
                delay(500)
                return if (count >= maxCount) {
                    null
                } else {
                    count++
                    listOf(subject)
                }
            }
        })
    }
    SubjectColumn(viewModel)
}