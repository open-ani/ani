package me.him188.ani.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.isMobile
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.foundation.layout.isShowLandscapeUI
import me.him188.ani.app.ui.foundation.pagerTabIndicatorOffset
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.tabs.AboutTab
import me.him188.ani.app.ui.settings.tabs.app.AppSettingsTab
import me.him188.ani.app.ui.settings.tabs.media.MediaPreferenceTab
import me.him188.ani.app.ui.settings.tabs.network.NetworkSettingsTab

/**
 * @see renderPreferenceTab 查看名称
 */
@Immutable
enum class SettingsTab {
    APP,
    MEDIA,
    NETWORK,
    ABOUT,
    ;

    companion object {
        @Stable
        val Default = MEDIA
    }
}

@Composable
fun SettingsPage(
    modifier: Modifier = Modifier,
    initialTab: SettingsTab = SettingsTab.Default,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    allowBack: Boolean = !isShowLandscapeUI(),
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    if (allowBack) {
                        TopAppBarGoBackButton()
                    }
                },
            )
        }
    ) { topBarPaddings ->
        val pagerState =
            rememberPagerState(initialPage = initialTab.ordinal) { SettingsTab.entries.size }
        val scope = rememberCoroutineScope()

        // Pager with TabRow
        Column(Modifier.padding(topBarPaddings).fillMaxSize()) {
            SecondaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = @Composable { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                SettingsTab.entries.forEachIndexed { index, tabId ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(text = renderPreferenceTab(tabId))
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                Modifier.fillMaxSize(),
                userScrollEnabled = Platform.currentPlatform.isMobile(),
            ) { index ->
                val type = SettingsTab.entries[index]
                Column(Modifier.fillMaxSize().padding(contentPadding)) {
                    when (type) {
                        SettingsTab.MEDIA -> MediaPreferenceTab(modifier = Modifier.fillMaxSize())
                        SettingsTab.NETWORK -> NetworkSettingsTab(modifier = Modifier.fillMaxSize())
                        SettingsTab.ABOUT -> AboutTab(modifier = Modifier.fillMaxSize())
                        SettingsTab.APP -> AppSettingsTab(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}


@Composable
private fun renderPreferenceTab(
    tab: SettingsTab,
): String {
    return when (tab) {
        SettingsTab.APP -> "应用与界面"
        SettingsTab.NETWORK -> "数据源与网络"
        SettingsTab.MEDIA -> "播放与缓存"
        SettingsTab.ABOUT -> "关于"
    }
}

@Composable
internal fun SettingsTab(
    modifier: Modifier = Modifier,
    content: @Composable SettingsScope.() -> Unit,
) {
    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val scope = remember(this) {
            object : SettingsScope(), ColumnScope by this {}
        }
        scope.content()
    }
}
