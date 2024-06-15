package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

@Preview
@Composable
internal fun PreviewSubjectDetails() {
    ProvideCompositionLocalsForPreview {
        val vm = remember {
            SubjectDetailsViewModel(400602)
        }
        SubjectDetailsPage(vm)
    }
}