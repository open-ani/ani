package me.him188.animationgarden.app.ui.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import me.him188.animationgarden.app.ui.home.MainScreenTab

/**
 * 主页的搜索页面 Tab
 */
class SearchTab(
    private val subjectDetailsNavigator: Navigator,
) : MainScreenTab {
    @Composable
    override fun Content() {
        SearchPage(subjectDetailsNavigator)
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "找番"
            val icon = rememberVectorPainter(Icons.Default.Search)
            return remember {
                TabOptions(
                    index = 1u,
                    title = title,
                    icon = icon
                )
            }
        }
}
