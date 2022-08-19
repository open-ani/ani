package me.him188.animationgarden.desktop.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LabelledRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: RadioButtonColors = RadioButtonDefaults.colors(),
    label: @Composable () -> Unit,
) {
    Row(
        modifier.then(
            if (onClick != null)
                Modifier.clickable(
                    interactionSource,
                    null,
                    onClick = onClick
                )
            else Modifier
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected, onClick, enabled = enabled, interactionSource = interactionSource, colors = colors)
        label()
    }
}
