package me.him188.ani.app.ui.subject.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
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
    val isDone by derivedStateOf {
        this.selfCollectionType == UnifiedCollectionType.DONE
    }

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

@Composable
fun EditableSubjectCollectionTypeIconButton(
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
    val type by rememberUpdatedState(state.selfCollectionType)

    val action by remember {
        derivedStateOf {
            SubjectCollectionActionsForCollect.find { it.type == type }
                ?: SubjectCollectionActions.Collect
        }
    }
    Box(modifier) {
        var showDropdown by rememberSaveable { mutableStateOf(false) }
        IconButton(
            onClick = {
                showDropdown = true
            },
            enabled = !state.isSetSelfCollectionTypeWorking,
        ) {
            action.icon()
        }
    }
}
