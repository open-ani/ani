package me.him188.ani.app.ui.subject.episode.video.settings

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter
import me.him188.ani.utils.platform.Uuid

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

internal fun createDanmakuRegexFilterState(): DanmakuRegexFilterState {
    val localListState = mutableStateOf(
        listOf(
            DanmakuRegexFilter(
                id = Uuid.randomString(),
                name = "测试",
                regex = "测试",
            ),
            DanmakuRegexFilter(
                id = Uuid.randomString(),
                name = "测试2",
                regex = "测试2",
            ),
        ),
    )
    val defaultAdd: (DanmakuRegexFilter) -> Unit = { filter ->
        localListState.value += filter
    }
    val defaultEdit: (String, DanmakuRegexFilter) -> Unit = { id, newFilter ->
        localListState.value = localListState.value.map {
            if (it.id == id) newFilter else it
        }
    }
    val defaultRemove: (DanmakuRegexFilter) -> Unit = { filter ->
        localListState.value -= filter
    }
    val defaultSwitch: (DanmakuRegexFilter) -> Unit = { filter ->
        localListState.value = localListState.value.map {
            if (it.id == filter.id) it.copy(enabled = !it.enabled) else it
        }
    }

    return DanmakuRegexFilterState(
        list = localListState,
        add = defaultAdd,
        edit = defaultEdit,
        remove = defaultRemove,
        switch = defaultSwitch,
    )
}

