package me.him188.ani.app.ui.foundation

import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = ripple(
        bounded = false,
        radius = 20.dp, /* IconButtonTokens.StateLayerSize / 2 */
    ),
    content: @Composable () -> Unit
) {
    @Suppress("DEPRECATION_ERROR")
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(40.dp/* IconButtonTokens.StateLayerSize */)
            .clip(CircleShape)
            .background(color = Color.Transparent)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = indication,
            ),
        contentAlignment = Alignment.Center,
    ) {
        val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}