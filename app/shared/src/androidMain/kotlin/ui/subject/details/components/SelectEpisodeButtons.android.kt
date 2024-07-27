package me.him188.ani.app.ui.subject.details.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.subject.collection.progress.ContinueWatchingStatus
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressInfo
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressState
import me.him188.ani.datasources.api.EpisodeSort

@Stable
object TestSubjectProgressInfos {
    @Stable
    val NotOnAir = SubjectProgressInfo(
        continueWatchingStatus = ContinueWatchingStatus.NotOnAir,
        nextEpisodeToPlay = null,
    )

    @Stable
    val ContinueWatching2 = SubjectProgressInfo(
        continueWatchingStatus = ContinueWatchingStatus.Continue(1, EpisodeSort(2)),
        nextEpisodeToPlay = null,
    )

    @Stable
    val Watched2 = SubjectProgressInfo(
        continueWatchingStatus = ContinueWatchingStatus.Watched(1, EpisodeSort(2)),
        nextEpisodeToPlay = null,
    )

    @Stable
    val Done = SubjectProgressInfo(
        continueWatchingStatus = ContinueWatchingStatus.Done,
        nextEpisodeToPlay = null,
    )
}

@Composable
fun rememberTestSubjectProgressState(
    info: SubjectProgressInfo = SubjectProgressInfo.Empty,
): SubjectProgressState {
    return remember {
        SubjectProgressState(
            stateOf(1),
            info = stateOf(info),
            episodeProgressInfos = mutableStateOf(emptyList()),
            onPlay = { _, _ -> },
        )
    }
}

@Composable
@PreviewLightDark
private fun PreviewSelectEpisodeButtonsDone() {
    ProvideCompositionLocalsForPreview {
        Surface {
            SubjectDetailsDefaults.SelectEpisodeButtons(
                rememberTestSubjectProgressState(
                    TestSubjectProgressInfos.Done,
                ),
                {},
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun PreviewSelectEpisodeButtonsContinue() {
    ProvideCompositionLocalsForPreview {
        Surface {
            SubjectDetailsDefaults.SelectEpisodeButtons(
                rememberTestSubjectProgressState(
                    TestSubjectProgressInfos.ContinueWatching2,
                ),
                {},
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun PreviewSelectEpisodeButtonsWatched() {
    ProvideCompositionLocalsForPreview {
        Surface {
            SubjectDetailsDefaults.SelectEpisodeButtons(
                rememberTestSubjectProgressState(
                    TestSubjectProgressInfos.Watched2,
                ),
                {},
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun PreviewSelectEpisodeButtonsNotOnAir() {
    ProvideCompositionLocalsForPreview {
        Surface {
            SubjectDetailsDefaults.SelectEpisodeButtons(
                rememberTestSubjectProgressState(
                    TestSubjectProgressInfos.NotOnAir,
                ),
                {},
            )
        }
    }
}
