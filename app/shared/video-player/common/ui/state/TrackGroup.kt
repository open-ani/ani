package me.him188.ani.app.videoplayer.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

@Stable
interface TrackGroup<T> {
    val current: StateFlow<T?>

    val candidates: Flow<List<T>>

    fun select(track: T?): Boolean
}

fun <T> emptyTrackGroup(): TrackGroup<T> = object : TrackGroup<T> {
    override val current: StateFlow<T?> = MutableStateFlow<T?>(null)
    override val candidates: Flow<List<T>> = emptyFlow()

    override fun select(track: T?): Boolean = false
}

@Immutable
interface Track

@Immutable
data class SubtitleTrack(
    val id: String,
    val internalId: String,
    val language: String?,
    val labels: List<Label>,
) : Track

@Immutable
data class Label(
    val language: String?, // "zh" 这指的是 value 的语言
    val value: String // "CHS", "简日", "繁日"
)

class MutableTrackGroup<T> : TrackGroup<T> {
    override val current: MutableStateFlow<T?> = MutableStateFlow(null)

    override val candidates: MutableStateFlow<List<T>> = MutableStateFlow(emptyList())

    override fun select(track: T?): Boolean {
        if (track == null) {
            current.value = null
            return true
        }
        if (track !in candidates.value) {
            return false
        }
        current.value = track
        return true
    }
}