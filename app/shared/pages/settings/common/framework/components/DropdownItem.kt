package me.him188.ani.app.ui.settings.framework.components

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

@SettingsDsl
@Composable
fun <T> SettingsScope.DropdownItem(
    selected: () -> T,
    values: () -> List<T>,
    itemText: @Composable (T) -> Unit,
    onSelect: (T) -> Unit,
    itemIcon: @Composable ((T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    description: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (RowScope.() -> Unit),
) {
    var showDropdown by rememberSaveable { mutableStateOf(false) }

    val selectedState by remember {
        derivedStateOf { selected() }
    }
    TextItem(
        title = title,
        modifier = modifier,
        description = description,
        icon = icon,
        action = {
            TextButton(onClick = { showDropdown = true }) {
                itemText(selectedState)
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
    )
}
