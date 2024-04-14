package me.him188.ani.app.ui.subject.cache

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import me.him188.ani.app.ui.subject.episode.mediaFetch.renderMediaSource
import me.him188.ani.datasources.core.cache.MediaCacheStorage

@Stable
class MediaCacheStorageSelectorState(
    val storages: List<MediaCacheStorage>,
) {
    val selectedStorage: MediaCacheStorage? by mutableStateOf(null)
}

@Composable
fun MediaCacheStorageSelector(
    state: MediaCacheStorageSelectorState,
    onSelect: (MediaCacheStorage) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RichDialogLayout(
        title = { Text("选择储存位置") },
        buttons = {
            TextButton(onDismissRequest) {
                Text("取消")
            }

            Button(
                { state.selectedStorage?.let(onSelect) },
                enabled = state.selectedStorage != null
            ) {
                Text("确认")
            }
        },
        modifier,
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { }
            items(state.storages, key = {}) { storage ->
                Column(Modifier.padding()) {
                    Text(renderMediaSource(storage.mediaSourceId))
                }
            }
            item { }
        }
    }
}