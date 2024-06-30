package me.him188.ani.app.ui.subject.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.api.topic.UnifiedCollectionType


@Composable
@Preview
fun PreviewCollectionActionButton() = ProvideCompositionLocalsForPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            for (entry in UnifiedCollectionType.entries) {
                CollectionActionButton(
                    type = entry,
                    onCollect = {},
                    onEdit = {},
                    collected = entry != UnifiedCollectionType.NOT_COLLECTED,
                )
            }
        }
    }
}
