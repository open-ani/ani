package me.him188.ani.app.ui.subject.episode.video.topbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import me.him188.ani.app.ui.foundation.TextWithBorder

@Composable
fun EpisodePlayerTitle(
    ep: String?,
    episodeTitle: String,
    subjectTitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.Start) {
        ProvideTextStyle(MaterialTheme.typography.titleMedium) {
            Row {
                TextWithBorder(
                    subjectTitle,
                    softWrap = false, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
        ProvideTextStyle(MaterialTheme.typography.titleSmall) {
            Row(Modifier, verticalAlignment = Alignment.CenterVertically) {
                // 过长时隐藏标题, 保留序号
                TextWithBorder(
                    remember(ep, episodeTitle) { (ep ?: "01") + "  " + episodeTitle },
                    softWrap = false, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }

        }
    }
}