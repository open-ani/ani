package me.him188.ani.app.ui.settings.framework.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.theme.stronglyWeaken

/**
 * 设置页的组件
 *
 * @see Group
 * @see TextItem
 * @see TextFieldItem
 * @see TextButtonItem
 * @see RowButtonItem
 * @see SliderItem
 * @see SwitchItem
 * @see SorterItem
 * @see DropdownItem
 */
@SettingsDsl
abstract class SettingsScope {
    @Stable
    @PublishedApi
    internal val itemHorizontalPadding = 16.dp

    @Stable
    @PublishedApi
    internal inline val labelAlpha get() = 0.8f

    @SettingsDsl
    @Composable
    fun Group(
        title: @Composable () -> Unit,
        description: (@Composable () -> Unit)? = null,
        modifier: Modifier = Modifier,
        useThinHeader: Boolean = false,
        actions: (@Composable RowScope.() -> Unit)? = null,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        Surface(modifier = modifier.fillMaxWidth()) {
            Column(Modifier.padding(vertical = if (useThinHeader) 12.dp else 16.dp)) {
                // Group header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier.padding(horizontal = itemHorizontalPadding)
                            .padding(bottom = 8.dp)
                            .weight(1f)
                            .heightIn(min = if (description != null) 48.dp else 24.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ProvideTextStyleContentColor(
                            MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        ) {
                            Row { title() }
                        }

                        description?.let {
                            ProvideTextStyleContentColor(
                                MaterialTheme.typography.labelMedium,
                                LocalContentColor.current.copy(labelAlpha),
                            ) {
                                Row(Modifier.padding()) { it() }
                            }
                        }
                    }

                    actions?.let {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                            Row(
                                Modifier.height(48.dp).padding(end = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                it()
                            }
                        }
                    }
                }

                // items
                content()
            }
        }
    }

    @Composable
    fun ItemHeader(
        title: @Composable RowScope.() -> Unit,
        description: @Composable (() -> Unit)?,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CompositionLocalProvider(
                LocalContentColor providesDefault MaterialTheme.colorScheme.onSurface
            ) {
                ProvideTextStyle(
                    MaterialTheme.typography.bodyLarge,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) { title() }
                }
            }
            ProvideTextStyleContentColor(
                MaterialTheme.typography.labelMedium,
                LocalContentColor.current.copy(labelAlpha)
            ) {
                description?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) { it() }
                }
            }
        }
    }

    @Composable
    inline fun SubGroup(
        content: () -> Unit,
    ) {
        Column(Modifier.padding(start = itemHorizontalPadding)) {
            content()
        }
    }

    @Composable
    fun Item(
        modifier: Modifier = Modifier,
        icon: @Composable (() -> Unit)? = null,
        action: @Composable (() -> Unit)? = null,
        content: @Composable () -> Unit = {},
    ) {
        Row(
            modifier
                .padding(horizontal = itemHorizontalPadding)
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(Modifier.padding(end = 8.dp).size(28.dp), contentAlignment = Alignment.Center) {
                    CompositionLocalProvider(LocalContentColor providesDefault MaterialTheme.colorScheme.onSurface) {
                        icon()
                    }
                }
            }

            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                content()
            }

            action?.let {
                Box(
                    Modifier.padding(start = 16.dp)
                        .widthIn(min = 48.dp), contentAlignment = Alignment.Center
                ) {
                    ProvideTextStyleContentColor(
                        MaterialTheme.typography.labelLarge,
                        MaterialTheme.colorScheme.primary
                    ) {
                        it()
                    }
                }
            }
        }
    }

    @SettingsDsl
    @Composable
    fun HorizontalDividerItem(
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.outlineVariant.stronglyWeaken()
    ) {
        Row(
            modifier
                .padding(horizontal = itemHorizontalPadding)
                .fillMaxWidth() // no min 48.dp height
        ) {
            HorizontalDivider(color = color)
        }
    }
}

@DslMarker
annotation class SettingsDsl
