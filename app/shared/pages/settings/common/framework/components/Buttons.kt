package me.him188.ani.app.ui.settings.framework.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

/**
 * 一个 [TextButton] 在最右侧
 */
@SettingsDsl
@Composable
fun SettingsScope.TextButtonItem(
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Item(
        modifier.clickable(onClick = onClick),
        action = {
            TextButton(onClick, enabled = enabled) {
                title()
            }
        }
    ) {
    }
}

/**
 * 一行字作为按钮
 */
@SettingsDsl
@Composable
fun SettingsScope.RowButtonItem(
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    description: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Item(
        modifier.clickable(onClick = onClick),
    ) {
        ItemHeader(
            title = {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                    title()
                }
            },
            description
        )
    }
}
