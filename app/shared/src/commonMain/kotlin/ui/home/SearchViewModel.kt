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

package me.him188.animationgarden.app.ui.home

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.him188.animationgarden.app.ui.framework.AbstractViewModel
import me.him188.animationgarden.app.ui.subject.SubjectListViewModel
import me.him188.animationgarden.datasources.api.SubjectProvider
import me.him188.animationgarden.datasources.api.SubjectSearchQuery
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SearchViewModel(
    keyword: String? = "葬送的芙莉莲", // TODO: for  debug 
) : AbstractViewModel(), KoinComponent {
    private val subjectProvider: SubjectProvider by inject()
    private val _result: MutableStateFlow<SubjectListViewModel?> = MutableStateFlow(null)


    val searchActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val editingQuery: MutableStateFlow<String> = MutableStateFlow(keyword ?: "")

    val result: StateFlow<SubjectListViewModel?> = _result

    init {
        keyword?.let { search(it) }
    }

    fun search(keywords: String) {
        if (keywords.isBlank()) {
            _result.value = null
            return
        }
        _result.value?.dispose()
        _result.value =
            SubjectListViewModel(
                subjectProvider.startSearch(SubjectSearchQuery(keywords.trim()))
            )
    }
}