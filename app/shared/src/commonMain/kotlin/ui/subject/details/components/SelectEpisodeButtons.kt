package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.subject.collection.progress.PlaySubjectButton
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressState

@Suppress("UnusedReceiverParameter")
@Composable
fun SubjectDetailsDefaults.SelectEpisodeButtons(
    state: SubjectProgressState,
    onShowEpisodeList: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onShowEpisodeList) {
            Icon(Icons.Outlined.Dataset, null)
        }

        Box(Modifier.weight(1f)) {
            PlaySubjectButton(state, Modifier.fillMaxWidth())
        }
    }
}
