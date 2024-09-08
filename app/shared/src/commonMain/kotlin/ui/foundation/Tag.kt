package me.him188.ani.app.ui.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.text.ProvideContentColor

@Composable
fun OutlinedTag(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    label: @Composable RowScope.() -> Unit,
) {
    Tag(
        modifier,
        leadingIcon,
        trailingIcon,
        shape,
        containerColor,
        contentColor,
        border,
        label,
    )
}

// 一个标签, 例如 "2023年10月", "漫画改"
@Composable
fun Tag(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    label: @Composable RowScope.() -> Unit,
) {
    // M3 Input chip
    // https://m3.material.io/components/chips/specs#facb7c02-74c4-4b81-bd52-6ad10ce351eb

    Surface(
        modifier
            .height(32.dp)
            .border(border, shape)
            .clip(shape),
        color = containerColor,
    ) {
        Row(
            Modifier.fillMaxHeight().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProvideContentColor(contentColor) {
                leadingIcon?.let {
                    Box(Modifier.size(18.dp)) {
                        it()
                    }
                }

                Row(Modifier.padding(horizontal = 8.dp)) {
                    ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                        label()
                    }
                }

                trailingIcon?.let {
                    Box(Modifier.size(18.dp)) {
                        it()
                    }
                }
            }
        }
    }
}
