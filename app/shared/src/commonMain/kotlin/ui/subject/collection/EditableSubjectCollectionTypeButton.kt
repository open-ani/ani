package me.him188.ani.app.ui.subject.collection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

@Stable
class EditableSubjectCollectionTypeState(
    selfCollectionType: State<UnifiedCollectionType>,
    private val hasAnyUnwatched: suspend () -> Boolean,
    private val onSetSelfCollectionType: suspend (UnifiedCollectionType) -> Unit,
    private val onSetAllEpisodesWatched: suspend () -> Unit,
    private val backgroundScope: CoroutineScope,
) {
    val selfCollectionType by selfCollectionType
    val isCollected by derivedStateOf {
        this.selfCollectionType != UnifiedCollectionType.NOT_COLLECTED
    }

    var showSetAllEpisodesDoneDialog by mutableStateOf(false)
    var showDropdown by mutableStateOf(false)

    private val setSelfCollectionTypeTasker = MonoTasker(backgroundScope)
    val isSetSelfCollectionTypeWorking get() = setSelfCollectionTypeTasker.isRunning

    fun setSelfCollectionType(new: UnifiedCollectionType) {
        setSelfCollectionTypeTasker.launch {
            onSetSelfCollectionType(new)
            if (new == UnifiedCollectionType.DONE && hasAnyUnwatched()) {
                withContext(Dispatchers.Main) { showSetAllEpisodesDoneDialog = true }
            }
        }
    }

    private val setAllEpisodesWatchedTasker = MonoTasker(backgroundScope)
    val isSetAllEpisodesWatchedWorking get() = setAllEpisodesWatchedTasker.isRunning

    fun setAllEpisodesWatched() {
        backgroundScope.launch { onSetAllEpisodesWatched() }
    }
}

@Composable
fun EditableSubjectCollectionTypeButton(
    state: EditableSubjectCollectionTypeState,
    modifier: Modifier = Modifier,
) {
    // 同时设置所有剧集为看过
    EditableSubjectCollectionTypeDialogsHost(state)

    SubjectCollectionTypeButton(
        state.selfCollectionType,
        onEdit = {
            state.setSelfCollectionType(it)
        },
        enabled = !state.isSetSelfCollectionTypeWorking,
        modifier = modifier,
    )
}

@Composable
fun EditableSubjectCollectionTypeDialogsHost(
    state: EditableSubjectCollectionTypeState,
) {
    // 同时设置所有剧集为看过
    if (state.showSetAllEpisodesDoneDialog) {
        SetAllEpisodeDoneDialog(
            onDismissRequest = { state.showSetAllEpisodesDoneDialog = false },
            isWorking = state.isSetAllEpisodesWatchedWorking,
            onConfirm = {
                state.setAllEpisodesWatched()
                state.showSetAllEpisodesDoneDialog = false
            },
        )
    }
}

