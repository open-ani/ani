package me.him188.ani.app.ui.subject.cache

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.media.cache.MediaCacheStorage
import me.him188.ani.app.ui.icons.renderMediaSource

@Composable
fun SelectMediaStorageDialog(
    options: List<MediaCacheStorage>,
    onSelect: (MediaCacheStorage) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("选择储存位置") },
        icon = { Icon(Icons.Rounded.Save, null) },
        confirmButton = {
            TextButton(onDismissRequest) {
                Text("取消")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(Modifier)
                for (storage in options) {
                    Column(Modifier.padding()) {
                        // TODO: 很丑, 但反正只在 debug 用
                        Card(
                            { onSelect(storage) },
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(renderMediaSource(storage.mediaSourceId)) // TODO: 本地的全都叫 "本地" 无法区分 
                        }
                    }
                }
                Spacer(Modifier)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun SelectMediaStorageDialogHost(state: EpisodeCacheRequesterPresentation, modifier: Modifier = Modifier) {
    state.selectStorageStage?.let { stage ->
        SelectMediaStorageDialog(
            options = stage.storages,
            onSelect = { state.selectStorage(it) },
            onDismissRequest = { state.cancelRequest() },
            modifier
        )
    }
}