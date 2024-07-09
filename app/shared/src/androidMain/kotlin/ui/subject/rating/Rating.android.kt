package me.him188.ani.app.ui.subject.rating

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.model.subject.RatingCounts
import me.him188.ani.app.data.model.subject.RatingInfo
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

internal val TestRatingInfo = RatingInfo(
    rank = 123,
    total = 100,
    count = RatingCounts(IntArray(10) { it * 10 }),
    score = "6.7",
)

@Composable
@Preview
fun PreviewRating() {
    ProvideCompositionLocalsForPreview {
        Rating(
            rating = TestRatingInfo,
            selfRatingScore = 0,
            {},
        )
    }
}

@Composable
@Preview
fun PreviewRatingWithSelf() {
    ProvideCompositionLocalsForPreview {
        Rating(
            rating = TestRatingInfo,
            selfRatingScore = 7,
            {},
        )
    }
}
