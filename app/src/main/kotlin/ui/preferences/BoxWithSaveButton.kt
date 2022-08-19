package me.him188.animationgarden.desktop.ui.preferences

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import me.him188.animationgarden.desktop.AppTheme
import me.him188.animationgarden.desktop.i18n.LocalI18n

@Composable
fun BoxWithSaveButton(
    showButton: Boolean,
    onClickSave: () -> Unit,
    buttonHeightOffset: @Composable () -> Dp,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    content: @Composable RowScope.(width: Dp) -> Unit,
) {
    BoxWithConstraints {
        Row(modifier) {
            val contentWeight by animateFloatAsState(if (showButton) 0.85f else 1.0f)

            content(contentWeight * this@BoxWithConstraints.maxWidth)

            Box(
                Modifier
                    .width((1 - contentWeight) * this@BoxWithConstraints.maxWidth)
                    .padding(start = 4.dp, top = 4.dp) // align to center
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    buttonModifier
                        .clip(AppTheme.shapes.small)
                        .width(40.dp)
                        .height(this@BoxWithConstraints.maxHeight + buttonHeightOffset())
                        .border(BorderStroke(1.dp, color = AppTheme.colorScheme.primary), shape = AppTheme.shapes.small)
                        .clickable(
                            remember { MutableInteractionSource() },
                            rememberRipple(),
                            onClick = onClickSave,
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource("drawable/check.svg"),
                        LocalI18n.current.getString("preferences.save.changes"),
                        Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
