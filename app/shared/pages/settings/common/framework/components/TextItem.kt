package me.him188.ani.app.ui.settings.framework.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

/**
 * Can become a text button if [onClick] is not null.
 */
@SettingsDsl
@Composable
fun SettingsScope.TextItem(
    title: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    description: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Item(
        modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        icon = kotlin.run {
            if (icon != null && onClick != null) {
                {
                    CompositionLocalProvider(LocalContentColor providesDefault MaterialTheme.colorScheme.primary) {
                        icon()
                    }
                }
            } else {
                icon
            }
        },
        action = action
    ) {
        if (onClick != null) {
            ItemHeader(
                {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                        title()
                    }
                },
                description,
                Modifier
            )
        } else {
            ItemHeader(title, description, Modifier)
        }
    }
}

