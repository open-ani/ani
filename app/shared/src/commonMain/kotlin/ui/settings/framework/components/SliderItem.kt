package me.him188.ani.app.ui.settings.framework.components

import androidx.annotation.IntRange
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@SettingsDsl
@Composable
fun SettingsScope.SliderItem(
    title: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    description: @Composable (() -> Unit)? = null,
    valueLabel: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Item(modifier) {
        Column {
            Row(
                Modifier,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ItemHeader(title, description, Modifier.weight(1f))

                valueLabel?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                            valueLabel()
                        }
                    }
                }
            }
            content()
        }
    }
}

@SettingsDsl
@Composable
fun SettingsScope.SliderItem(
    value: Float,
    onValueChange: (Float) -> Unit,
    title: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0)
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    valueLabel: @Composable (() -> Unit)? = {
        Text(value.toString())
    },
    description: @Composable (() -> Unit)? = null,
) {
    SliderItem(title, modifier, description, valueLabel) {
        Slider(
            value,
            onValueChange,
            Modifier,
            enabled,
            valueRange,
            steps,
            onValueChangeFinished,
            colors,
            interactionSource
        )
    }
}
