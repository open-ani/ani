package me.him188.animationgarden.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import me.him188.animationgarden.app.ui.foundation.TabNavigationItem
import me.him188.animationgarden.app.ui.search.SearchTab

/**
 * 主页的 tab
 */
interface MainScreenTab : Tab

object MainScreen : Screen {
    private fun readResolve(): Any = MainScreen

    @Composable
    override fun Content() {
        val subjectDetailsNavigator = LocalNavigator.currentOrThrow
        val searchTab = remember { SearchTab(subjectDetailsNavigator) }
        TabNavigator(tab = searchTab) {
            Scaffold(
                Modifier.statusBarsPadding(),
                bottomBar = {
                    Column(Modifier.alpha(0.99f)) {
                        Divider(Modifier.fillMaxWidth(), thickness = 1.dp)
                        BottomAppBar(
                            Modifier.height(64.dp),
                            containerColor = MaterialTheme.colorScheme.background,
                        ) {
                            TabNavigationItem(HomeTab)
                            TabNavigationItem(searchTab)
                        }
                    }
                }
            ) { paddingValues ->
                CompositionLocalProvider(LocalContentPaddings provides paddingValues) {
                    CurrentTab()
                }
            }
        }
    }
}

/**
 * 由 bottom bar 等导致的 paddings
 */
val LocalContentPaddings: ProvidableCompositionLocal<PaddingValues> = androidx.compose.runtime.compositionLocalOf {
    error("LocalContentPaddings is not provided")
}