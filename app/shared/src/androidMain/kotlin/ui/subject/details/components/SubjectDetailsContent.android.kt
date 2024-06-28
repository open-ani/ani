package me.him188.ani.app.ui.subject.details.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.details.TestSubjectCharacterList
import me.him188.ani.app.ui.subject.details.TestSubjectStaffInfo

@Composable
@Preview
private fun PreviewDetailsTab() {
    ProvideCompositionLocalsForPreview {
        SubjectDetailsDefaults.DetailsTab(
            TestSubjectInfo,
            TestSubjectStaffInfo,
            TestSubjectCharacterList,
        )
    }
}