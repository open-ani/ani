package me.him188.ani.app.ui.cache.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize

/**
 * 一个组缓存的共同信息, 例如一个条目的十几个剧集的缓存 [CachedMedia] 都来自一个季度全集 [Media], 那它们就有相同的 [CacheGroupCommonInfo].
 */
@Immutable
class CacheGroupCommonInfo(
    val subjectDisplayName: String,
)

@Stable
class CacheGroupState(
    /**
     * 共同属于的 media.
     */
    val media: Media,
    commonInfo: State<CacheGroupCommonInfo?>, // null means loading
    val episodes: List<CacheEpisodeState>,
    /**
     * 下载速度, 每秒. 对于不支持下载的缓存, 该值为 [FileSize.Zero].
     *
     * - 若 emit [FileSize.Unspecified], 表示上传速度未知. 这只会在该缓存正在上传, 但无法知道具体速度时出现.
     * - 若 emit [FileSize.Zero], 表示上传速度真的是零.
     */
    downloadSpeed: State<FileSize>,
    downloadedSize: State<FileSize>,
    /**
     * 上传速度, 每秒. 对于不支持上传的缓存, 该值为 [FileSize.Zero].
     *
     * - 若 emit [FileSize.Unspecified], 表示上传速度未知. 这只会在该缓存正在上传, 但无法知道具体速度时出现.
     * - 若 emit [FileSize.Zero], 表示上传速度真的是零.
     */
    uploadSpeed: State<FileSize>,
) {
    val downloadSpeedText by derivedStateOf {
        computeSpeedText(speed = downloadSpeed.value, size = downloadedSize.value)
    }

    val uploadSpeedText by derivedStateOf {
        computeSpeedText(speed = uploadSpeed.value, size = FileSize.Unspecified)
    }

    private val commonInfo by commonInfo
    var expanded: Boolean by mutableStateOf(true)

    val cardTitle by derivedStateOf {
        this.commonInfo?.subjectDisplayName ?: media.originalTitle
    }

    val cardSubtitle by derivedStateOf {
        if (expanded) {
            if (this.commonInfo?.subjectDisplayName == null) {
                null
            } else {
                media.originalTitle
            }
        } else {
            EpisodeRange.range(episodes.map { it.sort }).toString()
        }
    }

    companion object {
        fun computeSpeedText(speed: FileSize, size: FileSize): String {
            return when {
                size == FileSize.Unspecified && speed == FileSize.Unspecified -> ""
                size == FileSize.Unspecified -> speed.toString()
                speed == FileSize.Unspecified -> size.toString()
                else -> "$size ($speed/s)"
            }
        }
    }
}

@Composable
fun CacheGroupCard(
    state: CacheGroupState,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 24.dp,
    shape: Shape = MaterialTheme.shapes.large,
) {
    Card(
        modifier,
        shape = shape,
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Card(
            Modifier.fillMaxWidth(),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Column(Modifier.padding(vertical = 20.dp)) {
                Row(Modifier.padding(horizontal = horizontalPadding)) {
                    ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                        Text(state.cardTitle)
                    }
                }

//                state.cardSubtitle?.let { subtitle ->
//                    Row(Modifier.padding(top = 12.dp).padding(horizontal = horizontalPadding)) {
//                        ProvideTextStyleContentColor(
//                            MaterialTheme.typography.labelLarge,
//                            MaterialTheme.colorScheme.onSurfaceVariant,
//                        ) {
//                            Text(subtitle)
//                        }
//                    }
//                }

                Spacer(Modifier.height(20.dp))

                FlowRow(
                    Modifier.padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.Start),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProvideTextStyleContentColor(MaterialTheme.typography.labelLarge) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Download, null)
                            Text(state.downloadSpeedText, softWrap = false)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Upload, null)
                            Text(state.uploadSpeedText, softWrap = false)
                        }
                    }
                }
            }
        }

        AnimatedVisibility(state.expanded) {
            Column(
                Modifier.padding(top = 8.dp, bottom = 16.dp)
                    .padding(horizontal = (horizontalPadding - 16.dp).coerceAtLeast(0.dp)),
            ) {
                for (episode in state.episodes) {
                    CacheEpisodeItem(episode)
                }
            }
        }
    }
}
