package me.him188.ani.app.ui.subject.collection

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.data.models.subject.SelfRatingInfo
import me.him188.ani.app.tools.caching.mutate
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.subject.episode.details.rememberTestAuthState

internal val TestSelfRatingInfo = SelfRatingInfo(
    score = 7,
    comment = "test",
    tags = listOf("My tag"),
    isPrivate = false,
)

@Composable
@Preview
private fun PreviewCollectionPage() {
    ProvideCompositionLocalsForPreview {
        rememberViewModel {
            MyCollectionsViewModelImpl().apply {
                val testData = TestSubjectCollections
                runBlocking {
                    collectionsByType.forEach { c ->
                        c.cache.mutate {
                            testData.filter { it.collectionType == c.type }
                        }
                    }
                }
            }
        }

        CollectionPage(
            onClickCaches = {},
            contentPadding = PaddingValues(0.dp),
        )
    }
}

@Composable
@PreviewLightDark
private fun PreviewCollectionPageUnauthorizedTips() {
    ProvideCompositionLocalsForPreview {
        Surface {
            CollectionPageUnauthorizedTips(rememberTestAuthState(false))
        }
    }
}
