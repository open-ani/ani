package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.details.TestRelatedSubjects
import me.him188.ani.app.ui.subject.details.TestSubjectCharacterList
import me.him188.ani.app.ui.subject.details.TestSubjectStaffInfo

@Composable
@PreviewLightDark
private fun PreviewDetailsTab() {
    ProvideCompositionLocalsForPreview {
        Scaffold {
            SubjectDetailsDefaults.DetailsTab(
                TestSubjectInfo,
                TestSubjectStaffInfo,
                TestSubjectCharacterList,
                TestRelatedSubjects,
                Modifier.padding(it),
            )
        }
    }
}