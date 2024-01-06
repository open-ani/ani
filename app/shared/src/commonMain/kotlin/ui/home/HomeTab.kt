package me.him188.animationgarden.app.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.TabOptions

data object HomeTab : MainScreenTab {
    private fun readResolve(): Any = HomeTab

    @Composable
    override fun Content() {
        HomePage()
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "首页"
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }
} 