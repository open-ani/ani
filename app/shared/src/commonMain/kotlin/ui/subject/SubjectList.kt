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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.animationgarden.shared.models.Subject

@Stable
class SubjectListViewModel {
    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val list: MutableStateFlow<List<Subject>> = MutableStateFlow(listOf())

    fun loadMore() {
        if (_loading.value) return
    }
}

/**
 * 番剧列表
 */
@Composable
fun SubjectList(viewModel: SubjectListViewModel) {
    Column {
    }
}


@Composable
@Preview
private fun PreviewSubjectList() {
    val viewModel = SubjectListViewModel()
    SubjectList(viewModel)
}