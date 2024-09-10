package me.him188.ani.app.ui.subject.collection

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.data.models.subject.SelfRatingInfo
import me.him188.ani.app.tools.caching.mutate
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

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
        viewModel {
            MyCollectionsViewModel().apply {
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

        WindowInsets.ime
        CollectionPane(
            onClickCaches = {},
        )
    }
}
