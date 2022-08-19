package me.him188.animationgarden.desktop.ui.interaction

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope

interface MenuBarProvider {
    @Composable
    fun FrameWindowScope.MenuBar(
        onClickPreferences: () -> Unit,
    )
}