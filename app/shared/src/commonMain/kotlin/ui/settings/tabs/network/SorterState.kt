package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import org.burnoutcrew.reorderable.ReorderableLazyListState


@Composable
fun <T> rememberSorterState(
    onComplete: (newOrder: List<T>) -> Unit,
): SorterState<T> {
    val uiScope = rememberCoroutineScope()
    val onCompleteUpdated by rememberUpdatedState(onComplete)
    val density = LocalDensity.current
    return remember(density, uiScope) {
        SorterState(
            onComplete = onCompleteUpdated,
            uiScope = uiScope,
            maxScrollPerFrame = with(density) { 20.dp.toPx() },
        )
    }
}


@Stable
class SorterState<T>(
    private val onComplete: (newOrder: List<T>) -> Unit,
    uiScope: CoroutineScope,
    maxScrollPerFrame: Float,
) {
    private var _sortingData: List<T>? by mutableStateOf(null)

    val listState = LazyListState()
    val reorderableState = ReorderableLazyListState(
        listState,
        uiScope,
        maxScrollPerFrame = maxScrollPerFrame,
        onMove = { from, to ->
            _sortingData = sortingData.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    val isSorting by derivedStateOf { _sortingData != null }

    val sortingData by derivedStateOf {
        _sortingData.orEmpty()
    }

    fun start(data: List<T>) {
        _sortingData = data
    }

    fun complete() {
        val currentData = _sortingData ?: return // We don't need to crash the app :)
        onComplete(currentData)
        _sortingData = null
    }

    fun cancel() {
        _sortingData = null
    }
}