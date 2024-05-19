package me.him188.ani.app.ui.subject.cache

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import me.him188.ani.app.ui.subject.episode.mediaFetch.renderMediaSource
import me.him188.ani.datasources.core.cache.MediaCacheStorage

@Stable
class MediaCacheStorageSelectorState(
    val storages: List<MediaCacheStorage>,
) {
    var selectedStorage: MediaCacheStorage? by mutableStateOf(null)
}

// 目前这个只在 debug 时有用
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
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(Modifier)
            for (storage in state.storages) {
                Column(Modifier.padding()) {
                    // TODO: 很丑, 但反正只在 debug 用
                    Crossfade(state.selectedStorage == storage) {
                        if (it) {
                            ElevatedCard(
                                { state.selectedStorage = null },
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text(renderMediaSource(storage.mediaSourceId)) // TODO: 本地的全都叫 "本地" 无法区分 
                            }
                        } else {
                            Card(
                                { state.selectedStorage = storage },
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text(renderMediaSource(storage.mediaSourceId)) // TODO: 本地的全都叫 "本地" 无法区分 
                            }
                        }
                    }
                }
            }
            Spacer(Modifier)
        }
        // java.lang.IllegalStateException: Vertically scrollable component was measured with an infinity maximum height constraints, which is disallowed. One of the common reasons is nesting layouts like LazyColumn and Column(Modifier.verticalScroll()). If you want to add a header before the list of items please add a header as a separate item() before the main items() inside the LazyColumn scope. There are could be other reasons for this to happen: your ComposeView was added into a LinearLayout with some weight, you applied Modifier.wrapContentSize(unbounded = true) or wrote a custom layout. Please try to remove the source of infinite constraints in the hierarchy above the scrolling container.
//        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
//            item { }
//            items(state.storages, key = {}) { storage ->
//                Column(Modifier.padding()) {
//                    Text(renderMediaSource(storage.mediaSourceId))
//                }
//            }
//            item { }
//        }
    }
}