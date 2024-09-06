package me.him188.ani.app.ui.settings.framework.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Can become a text button if [onClick] is not null.
 * @param icon 放在最左边的图标
 * @param action 放在最右边的按钮, 例如 [IconButton]
 */
@SettingsDsl
@Composable
fun SettingsScope.TextItem(
    modifier: Modifier = Modifier,
    description: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onClickEnabled: Boolean = true,
    title: @Composable () -> Unit,
) {
    Item(
        headlineContent = title,
        modifier
            .then(onClick?.let { Modifier.clickable(onClickEnabled, onClick = it) } ?: Modifier),
        supportingContent = description,
        leadingContent = icon?.let {
            {
                SettingsDefaults.ItemIcon {
                    icon()
                }
            }
        },
        trailingContent = action,
    )
//    Item(
//        modifier
//            .then(if (onClick != null) Modifier.clickable(onClickEnabled, onClick = onClick) else Modifier),
//        icon = icon,
//        action = action,
//    ) {
//        ItemHeader(title, description, Modifier)
//    }
}

