package me.him188.ani.app.ui.subject.details.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.subject.SubjectCollectionStats
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

internal val TestCollectionStats = SubjectCollectionStats(
    wish = 100,
    doing = 200,
    done = 300,
    onHold = 400,
    dropped = 500,
)

internal val TestSubjectInfo = SubjectInfo(
    nameCn = "孤独摇滚！",
    name = "ぼっち・ざ・ろっく！",
    date = "2023-10-01",
)

internal const val TestCoverImage = "https://ui-avatars.com/api/?name=John+Doe"

@Composable
@Preview
fun PreviewSubjectDetailsHeader() {
    ProvideCompositionLocalsForPreview {
        SubjectDetailsHeader(TestSubjectInfo, TestCoverImage)
    }
}

