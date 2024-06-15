package me.him188.ani.app.ui.settings.framework.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * 下来菜单, 用于显示简单的选择. 例如选择主题是深色还是浅色.
 */
@SettingsDsl
@Composable
fun <T> SettingsScope.DropdownItem(
    selected: () -> T,
    values: () -> List<T>,
    itemText: @Composable (T) -> Unit,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemIcon: @Composable ((T) -> Unit)? = null,
    description: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (RowScope.() -> Unit),
    exposedItemText: @Composable (T) -> Unit = itemText,
    enabled: Boolean = true,
) {
    var showDropdown by rememberSaveable { mutableStateOf(false) }

    val selectedState by remember {
        derivedStateOf { selected() }
    }
    TextItem(
        modifier = modifier.clickable(onClick = { showDropdown = true }),
        description = description,
        icon = icon,
        action = {
            TextButton(onClick = { showDropdown = true }, enabled = enabled) {
                exposedItemText(selectedState)
            }
            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
            ) {
                values().forEach { value ->
                    val color = if (value == selectedState) {
                        MaterialTheme.colorScheme.primary
                    } else Color.Unspecified
                    CompositionLocalProvider(LocalContentColor providesDefault color) {
                        DropdownMenuItem(
                            text = { itemText(value) },
                            leadingIcon = if (itemIcon != null) {
                                {
                                    itemIcon(value)
                                }
                            } else null,
                            onClick = {
                                onSelect(value)
                                showDropdown = false
                            }
                        )
                    }
                }
            }
        },
        title = title,
    )
}
