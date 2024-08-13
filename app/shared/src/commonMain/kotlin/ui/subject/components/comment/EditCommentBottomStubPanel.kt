package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/**
 * 显示在评论列表底部的面板，用于打开 [EditComment]
 */
@Composable
fun EditCommentBottomStubPanel(
    text: TextFieldValue,
    onClickEditText: () -> Unit,
    onClickEmoji: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = "",
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.weight(1.0f)) {
                EditCommentDefaults.EditText(
                    value = text,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().focusProperties { canFocus = false },
                    onValueChange = { },
                    placeholder = if (hint != null) {
                        { Text(hint) }
                    } else null,
                )
                Spacer(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(onClick = onClickEditText),
                )
            }
            EditCommentDefaults.ActionButton(
                imageVector = Icons.Outlined.SentimentSatisfied,
                onClick = onClickEmoji,
                modifier = Modifier.size(EditCommentDefaults.ActionButtonSize.dp),
                enabled = true,
            )
        }
    }
}