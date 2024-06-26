package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.details.components.DetailsTab
import me.him188.ani.app.ui.subject.details.components.SubjectDetailsCollectionData
import me.him188.ani.app.ui.subject.details.components.SubjectDetailsDefaults
import me.him188.ani.app.ui.subject.details.components.TestSubjectInfo
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

@Preview
@Composable
internal fun PreviewSubjectDetails() {
    ProvideCompositionLocalsForPreview {
        val vm = remember {
            SubjectDetailsViewModel(400602)
        }
        val state = remember {
            SubjectDetailsState(
                subjectInfo = MutableStateFlow(TestSubjectInfo),
                coverImageUrl = "https://ui-avatars.com/api/?name=John+Doe",
                selfCollectionType = MutableStateFlow(UnifiedCollectionType.WISH),
                characters = MutableStateFlow(emptyList()),
                parentCoroutineContext = vm.backgroundScope.coroutineContext,
            )
        }
        SubjectDetailsPage(
            state = state,
            onClickOpenExternal = {},
            collectionData = {
                SubjectDetailsCollectionData(
                    vm.subjectDetailsState.info,
                    vm.subjectDetailsState.selfCollectionType,
                    onClickSelectEpisode = { },
                    onSetAllEpisodesDone = { vm.setAllEpisodesWatched() },
                    onSetCollectionType = { vm.setSelfCollectionType(it) },
                )
            },
            detailsTab = {
                SubjectDetailsDefaults.DetailsTab()
            },
            commentsTab = {},
            discussionsTab = {},
        )
    }
}