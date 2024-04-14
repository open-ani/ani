package me.him188.ani.app.ui.subject.cache

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelector
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorState
import me.him188.ani.datasources.api.Media

@Composable
fun EpisodeCacheMediaSelector(
    state: MediaSelectorState,
    onSelect: (Media) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    progressProvider: () -> Float? = { 1f },
) {
    MediaSelector(
        state,
        modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .navigationBarsPadding(),
        progressProvider = progressProvider,
        actions = {
            FilledTonalButton(onCancel) {
                Text("取消")
            }
        },
        onClickItem = {
            onSelect(it)
        }
    )
}