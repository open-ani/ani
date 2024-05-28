package me.him188.ani.app.ui.main

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.platform.showTabletUI
import me.him188.ani.app.ui.foundation.layout.LayoutMode
import me.him188.ani.app.ui.foundation.layout.LocalLayoutMode
import moe.tlaster.precompose.navigation.rememberNavigator

/**
 * 由 bottom bar 等导致的 paddings
 */ // TODO: remove LocalContentPaddings 
val LocalContentPaddings: ProvidableCompositionLocal<PaddingValues> = androidx.compose.runtime.compositionLocalOf {
    PaddingValues(0.dp)
}

/**
 * UI 入口点. 包含所有子页面, 以及组合这些子页面的方式 (navigation).
 */
@Composable
fun AniAppContent(aniNavigator: AniNavigator) {
    val navigator = rememberNavigator()
    SideEffect {
        aniNavigator.setNavigator(navigator)
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isLandscape = showTabletUI()
        val layoutMode = remember(isLandscape) {
            LayoutMode(isLandscape)
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
