package me.him188.ani.app.ui.subject.collection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import kotlin.coroutines.CoroutineContext

@Stable
class EditableSubjectCollectionTypeState(
    selfCollectionType: State<UnifiedCollectionType>,
    private val hasAnyUnwatched: () -> Boolean,
    private val onSetSelfCollectionType: suspend (UnifiedCollectionType) -> Unit,
    private val onSetAllEpisodesWatched: suspend () -> Unit,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    val selfCollectionType by selfCollectionType

    var showSetAllEpisodesDoneDialog by mutableStateOf(false)

    private val setSelfCollectionTypeTasker = MonoTasker(backgroundScope)
    val isSetSelfCollectionTypeWorking get() = setSelfCollectionTypeTasker.isRunning

    fun setSelfCollectionType(new: UnifiedCollectionType) {
        setSelfCollectionTypeTasker.launch {
            onSetSelfCollectionType(new)
        }
        if (new == UnifiedCollectionType.DONE && hasAnyUnwatched()) {
            showSetAllEpisodesDoneDialog = true
        }
    }

    fun setAllEpisodesWatched() {
        launchInBackground { onSetAllEpisodesWatched() }
    }
}

@Composable
fun EditableSubjectCollectionTypeButton(
    state: EditableSubjectCollectionTypeState,
    modifier: Modifier = Modifier,
) {
    // 同时设置所有剧集为看过
    if (state.showSetAllEpisodesDoneDialog) {
        SetAllEpisodeDoneDialog(
            onDismissRequest = { state.showSetAllEpisodesDoneDialog = false },
            onConfirm = {
                state.setAllEpisodesWatched()
                state.showSetAllEpisodesDoneDialog = false
            },
        )
    }

    SubjectCollectionTypeButton(
        state.selfCollectionType,
        onEdit = {
            state.setSelfCollectionType(it)
        },
        enabled = !state.isSetSelfCollectionTypeWorking,
        modifier = modifier,
    )
}
