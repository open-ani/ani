package me.him188.ani.app.ui.subject.cache

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorView
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSourceResultsPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberMediaSelectorPresentation
import me.him188.ani.datasources.api.Media

/**
 * 选择数据源来缓存
 * @param onSelect 当用户点击一个资源时调用
 * @param onCancel 当用户点击"取消"时调用
 */
@Composable
fun EpisodeCacheMediaSelector(
    state: MediaSelectorPresentation,
    onSelect: (Media) -> Unit,
    onCancel: () -> Unit,
    sourceResults: MediaSourceResultsPresentation,
    modifier: Modifier = Modifier,
) {
    MediaSelectorView(
        state,
        sourceResults = sourceResults,
        modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth(),
        onClickItem = {
            onSelect(it)
        },
        actions = {
            OutlinedButton(onCancel) {
                Text("取消")
            }
        },
    )
}

@Composable
fun EpisodeCacheMediaSelectorSheetHost(
    state: EpisodeCacheRequesterPresentation,
    modifier: Modifier = Modifier,
) {
    state.workingStage?.let { stage ->
        ModalBottomSheet(
            { state.cancelRequest() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = modifier,
        ) {
            EpisodeCacheMediaSelector(
                rememberMediaSelectorPresentation { stage.mediaSelector },
                onSelect = { media ->
                    state.selectMedia(media)
                },
                onCancel = { state.cancelRequest() },
                sourceResults = state.mediaSourceResults,
                Modifier.fillMaxHeight()// 防止添加筛选后数量变少导致 bottom sheet 高度变化
                    .navigationBarsPadding()
            )
        }
    }
}
