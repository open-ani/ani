package me.him188.ani.app.ui.subject.collection.progress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.models.subject.setEpisodeWatched
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.tools.caching.ContentPolicy
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import me.him188.ani.utils.coroutines.retryUntilSuccess
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
interface EpisodeProgressState {
    val subjectId: Int
    val title: String

    val theme: EpisodeProgressTheme

    val subjectProgress: List<EpisodeProgressItem>

    val hasAnyUnwatched: Boolean

    fun toggleEpisodeWatched(item: EpisodeProgressItem)
}

fun EpisodeProgressState(
    subjectId: Int,
    backgroundScope: HasBackgroundScope,
): EpisodeProgressState = EpisodeProgressStateImpl(subjectId, backgroundScope)

@Composable
fun rememberEpisodeProgressState(
    subjectId: Int,
): EpisodeProgressState {
    val scope = rememberBackgroundScope()
    return remember(subjectId, scope) {
        EpisodeProgressState(subjectId, scope)
    }
}

@Stable
class EpisodeProgressStateImpl(
    override val subjectId: Int,
    backgroundScope: HasBackgroundScope,
) : EpisodeProgressState, HasBackgroundScope by backgroundScope, KoinComponent {
    private val subjectManager: SubjectManager by inject()
    private val settingsRepository: SettingsRepository by inject()

    private val subject by flow {
        emit(subjectManager.getSubjectInfo(subjectId))
    }.retryUntilSuccess().produceState(null)

    override val title: String by derivedStateOf { subject?.displayName ?: "" }
    override val theme: EpisodeProgressTheme by settingsRepository.uiSettings.flow
        .map { it.episodeProgress.theme }
        .distinctUntilChanged()
        .produceState(EpisodeProgressTheme.Default)

    override val subjectProgress: List<EpisodeProgressItem> by subjectManager
        .subjectProgressFlow(subjectId, ContentPolicy.CACHE_ONLY)
        .produceState(emptyList())
    override val hasAnyUnwatched: Boolean by derivedStateOf {
        subjectProgress.any { !it.watchStatus.isDoneOrDropped() }
    }

    override fun toggleEpisodeWatched(item: EpisodeProgressItem) {
        if (item.isLoading) return
        launchInBackground {
            try {
                subjectManager.setEpisodeWatched(
                    subjectId,
                    episodeId = item.episodeId,
                    watched = item.watchStatus != UnifiedCollectionType.DONE,
                )
            } finally {
                item.isLoading = false
            }
        }
    }
}
