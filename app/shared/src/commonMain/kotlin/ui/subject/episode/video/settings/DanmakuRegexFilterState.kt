package me.him188.ani.app.ui.subject.episode.video.settings

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter

@Stable
class DanmakuRegexFilterState(
    list: State<List<DanmakuRegexFilter>>,
    val add: (filter: DanmakuRegexFilter) -> Unit,
    val edit: (id: String, new: DanmakuRegexFilter) -> Unit,
    val remove: (filter: DanmakuRegexFilter) -> Unit,
    val switch: (filter: DanmakuRegexFilter) -> Unit,
) {
    val list by list
}