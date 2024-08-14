package me.him188.ani.app.ui.foundation.interaction

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.hoverable(
    onHover: () -> Unit,
    onUnhover: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val onHoverUpdated by rememberUpdatedState(onHover)
    val onUnhoverUpdated by rememberUpdatedState(onUnhover)
    LaunchedEffect(true) {
        val hoverInteractions = mutableListOf<HoverInteraction.Enter>()
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is HoverInteraction.Enter -> hoverInteractions.add(interaction)
                is HoverInteraction.Exit -> hoverInteractions.remove(interaction.enter)
            }
            val isHovered = hoverInteractions.isNotEmpty()
            if (isHovered) {
                onHoverUpdated()
            } else {
                onUnhoverUpdated()
            }
        }
    }
    hoverable(interactionSource)
}