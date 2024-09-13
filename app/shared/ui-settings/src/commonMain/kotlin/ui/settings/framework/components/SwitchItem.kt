package me.him188.ani.app.ui.settings.framework.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


/**
 * A switch item that only the switch is interactable.
 */
@SettingsDsl
@Composable
fun SettingsScope.SwitchItem(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: @Composable (() -> Unit)? = null,
    switch: @Composable () -> Unit,
) {
    Item(
        headlineContent = title,
        modifier = modifier,
        supportingContent = description,
        trailingContent = switch,
    )
//    Item(modifier) {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            ItemHeader(title, description, Modifier.weight(1f).padding(end = 16.dp))
//            switch()
//        }
//    }
}


/**
 * A switch item that the entire item is clickable.
 */
@SettingsDsl
@Composable
fun SettingsScope.SwitchItem(
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: @Composable (() -> Unit)? = null,
    switch: @Composable () -> Unit,
) {
    SwitchItem(
        title, modifier.clickable(onClick = onClick), description, switch,
    )
}

/**
 * A switch item that the entire item is clickable.
 */
@SettingsDsl
@Composable
fun SettingsScope.SwitchItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    SwitchItem(
        { if (enabled) onCheckedChange(!checked) },
        title,
        modifier,
        description,
    ) {
        Switch(
            checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

