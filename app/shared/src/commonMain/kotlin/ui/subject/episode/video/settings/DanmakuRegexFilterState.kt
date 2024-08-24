package me.him188.ani.app.ui.subject.episode.video.settings

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter

@Stable
class DanmakuRegexFilterState(
    danmakuRegexFilterList: State<List<DanmakuRegexFilter>>,
    val addDanmakuRegexFilter: (filter: DanmakuRegexFilter) -> Unit,
    val editDanmakuRegexFilter: (id: String, new: DanmakuRegexFilter) -> Unit,
    val removeDanmakuRegexFilter: (filter: DanmakuRegexFilter) -> Unit,
    val switchDanmakuRegexFilter: (filter: DanmakuRegexFilter) -> Unit,
) {
    val danmakuRegexFilterList by danmakuRegexFilterList
}