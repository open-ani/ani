package me.him188.ani.app.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
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
    val shape = RoundedCornerShape(16.dp)
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
        modifier
            .offset(y = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = shape)
            .offset(y = (-4).dp),
        shape = shape,
        placeholder = { Text("搜索") },
        leadingIcon = { Icon(Icons.Outlined.Search, null) },
        trailingIcon = {
            if (searchActive) {
                IconButton({
                    viewModel.editingQuery.value = ""
                    viewModel.searchActive.value = false
                }) {
                    Icon(Icons.Outlined.Close, "取消")
                }
            }
        },
        colors = SearchBarDefaults.colors(
            containerColor = Color.Transparent,
            dividerColor = MaterialTheme.colorScheme.outline.weaken(),
        ),
    ) {
        Column(Modifier.fillMaxSize()) {
            val result by viewModel.result.collectAsStateWithLifecycle()
            result?.let {
                SubjectPreviewColumn(it)
            }
        }
    }
}
