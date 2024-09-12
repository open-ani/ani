package me.him188.ani.app.ui.subject.details.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.data.models.subject.ContinueWatchingStatus
import me.him188.ani.app.data.models.subject.SubjectProgressInfo
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressState
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.PackedDate
import me.him188.ani.utils.platform.annotations.TestOnly

@Stable
@TestOnly
object TestSubjectProgressInfos {
    @Stable
    val NotOnAir = SubjectProgressInfo(
        continueWatchingStatus = ContinueWatchingStatus.NotOnAir(PackedDate.Invalid),
        nextEpisodeIdToPlay = null,
    )

    @Stable
    val ContinueWatching2 = SubjectProgressInfo(
        continueWatchingStatus = ContinueWatchingStatus.Continue(1, EpisodeSort(2), EpisodeSort(1)),
        nextEpisodeIdToPlay = null,
    )

    @Stable
    val Watched2 = SubjectProgressInfo(
        continueWatchingStatus = ContinueWatchingStatus.Watched(1, EpisodeSort(2), PackedDate.Invalid),
        nextEpisodeIdToPlay = null,
    )

    @Stable
    val Done = SubjectProgressInfo(
        continueWatchingStatus = ContinueWatchingStatus.Done,
        nextEpisodeIdToPlay = null,
    )
}

@Composable
@TestOnly
fun rememberTestSubjectProgressState(
    info: SubjectProgressInfo = SubjectProgressInfo.Done,
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

@OptIn(TestOnly::class)
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

@OptIn(TestOnly::class)
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

@OptIn(TestOnly::class)
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

@OptIn(TestOnly::class)
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
