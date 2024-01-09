package me.him188.ani.app.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import me.him188.ani.app.ui.subject.SubjectPreviewColumn
import me.him188.ani.app.ui.theme.weaken
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun SubjectSearchBar(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier,
) {
    val query by viewModel.editingQuery.collectAsStateWithLifecycle()
    val searchActive by viewModel.searchActive.collectAsStateWithLifecycle()
    val keyboard by rememberUpdatedState(LocalSoftwareKeyboardController.current)
    SearchBar(
        query,
        onQueryChange = {
            viewModel.editingQuery.value = it
        },
        onSearch = {
            viewModel.search(it)
            keyboard?.hide()
        },
        searchActive,
        onActiveChange = { viewModel.searchActive.value = it },
        modifier.fillMaxWidth(),
        placeholder = { Text("搜索") },
        leadingIcon = { Icon(Icons.Outlined.Search, null) },
        trailingIcon = {
            if (searchActive) {
                IconButton({
                    viewModel.editingQuery.value = ""
                    viewModel.searchActive.value = false
                }) {
                    Icon(Icons.Outlined.Close, "Cancel")
                }
            }
        },
        colors = SearchBarDefaults.colors(dividerColor = MaterialTheme.colorScheme.outline.weaken()),
    ) {
        val result by viewModel.result.collectAsStateWithLifecycle()
        result?.let {
            SubjectPreviewColumn(it)
        }
    }
}
