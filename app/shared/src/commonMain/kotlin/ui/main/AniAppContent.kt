package me.him188.ani.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.navigation.compose.rememberNavController
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.platform.showTabletUI
import me.him188.ani.app.ui.foundation.layout.LayoutMode
import me.him188.ani.app.ui.foundation.layout.LocalLayoutMode

/**
 * UI 入口点. 包含所有子页面, 以及组合这些子页面的方式 (navigation).
 */
@Composable
fun AniAppContent(aniNavigator: AniNavigator) {
    val navigator = rememberNavController()
    SideEffect {
        aniNavigator.setNavController(navigator)
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isLandscape by rememberUpdatedState(showTabletUI())
        val size by rememberUpdatedState(
            with(LocalDensity.current) {
                DpSize(constraints.maxWidth.toDp(), constraints.maxHeight.toDp())
            },
        )
        val layoutMode by remember {
            derivedStateOf { LayoutMode(isLandscape, size) }
        }
        CompositionLocalProvider(LocalLayoutMode provides layoutMode) {
//            if (isLandscape) {
//                AniAppContentLandscape(aniNavigator, Modifier.fillMaxSize())
//            } else {
            AniAppContentPortrait(aniNavigator, Modifier.fillMaxSize())
//            }
        }
    }
}
