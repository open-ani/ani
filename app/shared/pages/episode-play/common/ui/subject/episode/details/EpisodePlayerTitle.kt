package me.him188.ani.app.ui.subject.episode.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun EpisodePlayerTitle(
    ep: String?,
    episodeTitle: String,
    subjectTitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.Start) {
        Row(Modifier, verticalAlignment = Alignment.CenterVertically) {
            // 过长时隐藏标题, 保留序号
            Text(
                remember(ep, episodeTitle) { (ep ?: "01") + "  " + episodeTitle },
                softWrap = false, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Row {
            Text(
                subjectTitle,
                style = MaterialTheme.typography.titleSmall,
                softWrap = false, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}