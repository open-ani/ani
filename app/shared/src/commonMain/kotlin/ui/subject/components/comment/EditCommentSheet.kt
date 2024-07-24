package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken


/**
 * 评论编辑
 *
 * @param title 可以表示状态, 例如: "评论: XXXX"
 */
@Composable
fun EditCommentSheet(
    actionRow: @Composable ColumnScope.() -> Unit,
    showExpand: Boolean = true,
    expanded: Boolean = false,
    modifier: Modifier = Modifier,
    onClickExpanded: (Boolean) -> Unit = { },
    title: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (title != null) title() else Spacer(Modifier)
            if (showExpand) {
                EditCommentSheetDefault.ActionButton(
                    imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    onClick = { onClickExpanded(!expanded) },
                )
            }

        }
        Spacer(modifier = Modifier.height(8.dp))

        CompositionLocalProvider(
            value = LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            content = { content() },
        )
        Spacer(modifier = Modifier.height(8.dp))
        actionRow()

    }
}

object EditCommentSheetDefault {
    @Composable
    fun Title(text: String) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }

    @Composable
    fun EditText(
        value: String,
        enabled: Boolean = true,
        hint: String? = null,
        maxLine: Int? = null,
        modifier: Modifier = Modifier,
        interactionSource: InteractionSource = remember { MutableInteractionSource() },
        onValueChange: (String) -> Unit
    ) {
        BasicTextField(
            value = value,
            textStyle = MaterialTheme.typography.bodyMedium.merge(
                fontSize = 15.5.sp,
                color = LocalContentColor.current.slightlyWeaken(),
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = modifier,
            maxLines = maxLine ?: Int.MAX_VALUE,
            onValueChange = onValueChange,
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = value,
                    enabled = enabled,
                    innerTextField = innerTextField,
                    interactionSource = interactionSource,
                    singleLine = false,
                    visualTransformation = VisualTransformation.None,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                )
            },
        )
    }

    @Composable
    fun ActionButton(imageVector: ImageVector, onClick: () -> Unit) {
        IconButton(
            modifier = Modifier.size(36.dp),
            onClick = onClick,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
    }

    @Composable
    fun ActionRow(
        onClickBold: () -> Unit = { },
        onClickItalic: () -> Unit = { },
        onClickUnderlined: () -> Unit = { },
        onClickStrikethrough: () -> Unit = { },
        onClickMask: () -> Unit = { },
        onClickImage: () -> Unit = { },
        onClickUrl: () -> Unit = { },
        onClickEmoji: () -> Unit = { },
        onSend: () -> Unit = { },
        onPreview: () -> Unit = { },
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LazyRow(modifier = Modifier.weight(1f)) {
                item { ActionButton(Icons.Outlined.SentimentSatisfied, onClickEmoji) }
                item { ActionButton(Icons.Outlined.FormatBold, onClickBold) }
                item { ActionButton(Icons.Outlined.FormatItalic, onClickItalic) }
                item { ActionButton(Icons.Outlined.FormatUnderlined, onClickUnderlined) }
                item { ActionButton(Icons.Outlined.FormatStrikethrough, onClickStrikethrough) }
                item { ActionButton(Icons.Outlined.VisibilityOff, onClickMask) }
                item { ActionButton(Icons.Outlined.Image, onClickImage) }
                item { ActionButton(Icons.Outlined.Link, onClickUrl) }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(start = 6.dp),
            ) {
                OutlinedButton(
                    onClick = onPreview,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Text(text = "预览", fontSize = MaterialTheme.typography.labelMedium.fontSize)
                }
                Button(
                    onClick = onSend,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Text(text = "发送", fontSize = MaterialTheme.typography.labelMedium.fontSize)
                }
            }
        }

    }
}