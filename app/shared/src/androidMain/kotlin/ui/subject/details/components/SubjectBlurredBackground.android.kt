package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

@Composable
@Preview
fun PreviewSubjectBlurredBackground() {
    // TODO:  PreviewSubjectBlurredBackground does not work
    ProvideCompositionLocalsForPreview {
        SubjectBlurredBackground(
            coverImageUrl = "https://ui-avatars.com/api/?name=John+Doe",
            Modifier
                .height(270.dp)
                .fillMaxWidth(),
        )
    }
}
