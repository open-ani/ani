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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.him188.animationgarden.app.preview.PreviewData
import me.him188.animationgarden.datasources.api.SearchSession
import me.him188.animationgarden.datasources.api.Subject

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

            private val subject get() = PreviewData.SosouNoFurilen
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
    SubjectPreviewColumn(viewModel)
}