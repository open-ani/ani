package me.him188.ani.app.ui.subject.cache

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.widgets.RichDialog
import me.him188.ani.datasources.api.EpisodeSort

@Composable
fun EditEpisodeCache(
    episodeSort: EpisodeSort,
    episodeTitle: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RichDialog(
        title = { Text("编辑缓存") },
        subtitle = {
            Text(episodeSort.toString())
            Text(episodeTitle, Modifier.padding(start = 8.dp))
        },
        buttons = {
            Button(onDismissRequest) {
                Text("完成")
            }
        },
        onDismissRequest = onDismissRequest,
        modifier,
    ) {
    }
}