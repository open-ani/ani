package me.him188.ani.app.ui.settings.framework.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun FoldableItem(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        title()
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            Column {
                content()
            }
        }
    }
}
