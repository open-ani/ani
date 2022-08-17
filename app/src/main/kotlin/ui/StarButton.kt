package me.him188.animationgarden.desktop.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.animationgarden.desktop.AppTheme

@Composable
fun ToggleStarButton(
    starred: Boolean,
    onStarredChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    normalSize: Dp = 24.dp,
    pressedSize: Dp = 26.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    IconToggleButton(
        checked = starred,
        onCheckedChange = onStarredChange,
        interactionSource = interaction,
        modifier = modifier,
    ) {
        val isHovered by interaction.collectIsHoveredAsState()
        val isPressed by interaction.collectIsPressedAsState()

        val enter = fadeIn(tween(150, easing = LinearOutSlowInEasing))
        val exit = fadeOut(tween(175, easing = LinearOutSlowInEasing))

        val boxSize by animateDpAsState(if (isPressed) pressedSize else normalSize) // 'bouncing' animation on click
        Box(Modifier.size(boxSize)) {
            AnimatedVisibility(
                starred && isHovered,
                enter = enter,
                exit = exit
            ) {
                Icon(
                    painterResource("drawable/star-remove.svg"),
                    "Remove Star",
                    tint = AppTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AnimatedVisibility(
                starred && !isHovered,
                enter = enter,
                exit = exit
            ) {
                Icon(
                    painterResource("drawable/star.svg"),
                    "Remove Star",
                    tint = AppTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                )
            }

            AnimatedVisibility(
                !starred && isHovered,
                enter = enter,
                exit = exit
            ) {
                Icon(
                    painterResource("drawable/star-plus-outline.svg"),
                    "Star",
                    tint = AppTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AnimatedVisibility(
                !starred && !isHovered,
                enter = enter,
                exit = exit
            ) {
                Icon(
                    painterResource("drawable/star-outline.svg"),
                    "Star",
                    tint = AppTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

//        Icon(
//            if (starred) {
//                if (isHovered) {
//                    painterResource("drawable/star-remove.svg")
//                } else {
//                    painterResource("drawable/star.svg")
//                }
//            } else {
//                if (isHovered) {
//                    painterResource("drawable/star-plus-outline.svg")
//                } else {
//                    painterResource("drawable/star-outline.svg")
//                }
//            },
//            if (starred) "Star" else "Remove Star",
//            tint = AppTheme.colorScheme.primary,
//        )
    }
}

@Preview
@Composable
private fun PreviewStarButton() {
    val (starred, onStarredChange) = remember { mutableStateOf(false) }
    ToggleStarButton(starred, onStarredChange)
}
