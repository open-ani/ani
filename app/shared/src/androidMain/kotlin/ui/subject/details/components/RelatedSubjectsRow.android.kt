package me.him188.ani.app.ui.subject.details.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.details.TestRelatedSubjects

@PreviewLightDark
@Composable
fun PreviewRelatedSubjectsRow() = ProvideCompositionLocalsForPreview {
    Surface {
        RelatedSubjectsRow(
            TestRelatedSubjects,
            onClick = {},
        )
    }
}
