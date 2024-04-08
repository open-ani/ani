package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.PreviewData
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

@Preview
@Composable
internal fun PreviewSubjectDetails() {
    ProvideCompositionLocalsForPreview {
        val vm = remember {
            SubjectDetailsViewModel(PreviewData.SOSOU_NO_FURILEN_SUBJECT_ID)
        }
        SubjectDetailsPage(vm, goBack = {})
    }
}