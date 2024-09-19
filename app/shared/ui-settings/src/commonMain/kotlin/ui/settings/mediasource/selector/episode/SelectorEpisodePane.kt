/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.episode

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import me.him188.ani.app.ui.foundation.layout.ConnectedScrollState
import me.him188.ani.app.ui.foundation.layout.paneHorizontalPadding
import me.him188.ani.app.ui.foundation.layout.rememberConnectedScrollState
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.widgets.FastLinearProgressIndicator
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.settings.mediasource.selector.EditSelectorMediaSourcePageState
import me.him188.ani.app.ui.settings.mediasource.selector.edit.SelectorConfigurationDefaults
import me.him188.ani.app.ui.settings.mediasource.selector.test.SelectorTestPane

@Composable
fun SelectorTestAndEpisodePane(
    state: EditSelectorMediaSourcePageState,
    layout: SelectorEpisodePaneLayout,
    modifier: Modifier = Modifier,
    nestedNav: NavHostController = rememberNavController(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    testConnectedScrollState: ConnectedScrollState = rememberConnectedScrollState(),
    initialRoute: SelectorEpisodePaneRoutes = SelectorEpisodePaneRoutes.TEST,
) {
    SharedTransitionScope { transitionModifier ->
        NavHost(nestedNav, initialRoute, modifier.then(transitionModifier)) {
            composable<SelectorEpisodePaneRoutes.TEST> {
                SelectorTestPane(
                    state.testState,
                    onViewEpisode = {
                        state.viewEpisode(it)
                    },
                    this,
                    Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                    connectedScrollState = testConnectedScrollState,
                )
            }
            composable<SelectorEpisodePaneRoutes.EPISODE> {
                BackHandler {
                    state.stopViewing()
                    nestedNav.popBackStack(SelectorEpisodePaneRoutes.EPISODE, inclusive = true)
                }
                val cardColors: CardColors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                )

                // decorate
                val content: @Composable () -> Unit = {
                    SelectorEpisodePaneContent(
                        state.episodeState,
                        Modifier.fillMaxSize(),
                        itemColors = ListItemDefaults.colors(containerColor = cardColors.containerColor),
                    )
                }
                val topAppBarDecorated = if (layout.showTopBarInPane) {
                    {
                        // list 展开, 能编辑配置
                        Card(
                            Modifier
                                .sharedBounds(rememberSharedContentState(state.episodeState.lastNonNullId), this)
                                .fillMaxSize(),
                            colors = cardColors,
                            shape = MaterialTheme.shapes.large,
                        ) {
                            SelectorEpisodePaneDefaults.TopAppBar(state.episodeState)
                            content()
                        }
                    }
                } else content

                val bottomSheetDecorated = if (layout.showBottomSheet) {
                    {
                        BottomSheetScaffold(
                            sheetContent = {
                                SelectorEpisodePaneDefaults.ConfigurationContent(
                                    state.configurationState,
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                )
                            },
                            Modifier
                                .fillMaxSize(),
                            sheetPeekHeight = 78.dp,
                        ) { paddingValues ->
                            Box(Modifier.padding(paddingValues)) {
                                topAppBarDecorated()
                            }
                        }
                    }
                } else topAppBarDecorated

                Box(Modifier.padding(contentPadding)) {
                    bottomSheetDecorated()
                }
            }
        }

        // 切换 item 时自动 nav
        LaunchedEffect(state) {
            snapshotFlow { state.viewingItem }.collect { value ->
                if (value == null) {
                    nestedNav.navigate(SelectorEpisodePaneRoutes.TEST)
                } else {
                    nestedNav.navigate(SelectorEpisodePaneRoutes.EPISODE)
                }
            }
        }
    }
}


@Composable
fun SelectorEpisodePaneContent(
    state: SelectorEpisodeState,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = SelectorConfigurationDefaults.verticalSpacing,
    horizontalPadding: Dp = currentWindowAdaptiveInfo().windowSizeClass.paneHorizontalPadding,
    itemColors: ListItemColors = ListItemDefaults.colors(),
) {
    Column(modifier) {
        Box(Modifier.height(4.dp), contentAlignment = Alignment.Center) {
            FastLinearProgressIndicator(
                state.isSearchingInProgress,
                delayMillis = 0,
                minimumDurationMillis = 300,
            )
        }

        val list by state.matchResults.collectAsStateWithLifecycle(emptyList())

        Row(
            Modifier.padding(
                start = horizontalPadding, end = horizontalPadding,
                top = 20.dp,
                bottom = 20.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val matchedSize by remember {
                derivedStateOf {
                    list.count { it.isMatch() }
                }
            }
            ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                when (matchedSize) {
                    0 -> {
                        Icon(
                            Icons.Rounded.PriorityHigh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Text("根据步骤 3 的配置，从 ${list.size} 个链接中未匹配到播放链接，请检查配置")
                    }

                    1 -> {
                        Icon(
                            Icons.Rounded.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text("根据步骤 3 的配置，从 ${list.size} 个链接中匹配到了 $matchedSize 个链接")
                    }

                    else -> {
                        Icon(
                            Icons.Rounded.PriorityHigh,
                            contentDescription = null,
                            tint = Color.Yellow.compositeOver(MaterialTheme.colorScheme.error),
                        )
                        Text("根据步骤 3 的配置，从 ${list.size} 个链接中匹配到了 $matchedSize 个链接。为了更好的稳定性，建议调整规则，匹配到正好一个链接")
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(
                bottom = itemSpacing,
                start = horizontalPadding - 8.dp, end = horizontalPadding,
            ),
        ) {
            // 上面总是有个东西可以保证当后面加载到匹配 (置顶) 时, 看到的是那个被匹配到的
            item { Spacer(Modifier.height(1.dp)) }

            for (matchResult in list) {
                item(key = matchResult.originalUrl) {
                    val isMatch = matchResult.isMatch()
                    val toaster = LocalToaster.current
                    val clipboard = LocalClipboardManager.current
                    ListItem(
                        headlineContent = {
                            Text(
                                matchResult.originalUrl,
                                color = if (isMatch) MaterialTheme.colorScheme.primary else Color.Unspecified,
                            )
                        },
                        Modifier.animateItem()
                            .clickable {
                                clipboard.setText(AnnotatedString(matchResult.originalUrl))
                                toaster.toast("已复制")
                            },
                        supportingContent = {
                            matchResult.video?.m3u8Url?.let {
                                if (it != matchResult.originalUrl) {
                                    Text("将实际播放：$it")
                                }
                            }
                        },
                        colors = itemColors,
                        leadingContent = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isMatch) {
                                    Icon(Icons.Rounded.Check, "匹配", tint = MaterialTheme.colorScheme.primary)
                                } else {
                                    Icon(Icons.Rounded.Close, "未匹配")
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}


@Serializable
sealed class SelectorEpisodePaneRoutes {
    @Serializable
    data object TEST : SelectorEpisodePaneRoutes()

    @Serializable
    data object EPISODE : SelectorEpisodePaneRoutes()
}

@Immutable
data class SelectorEpisodePaneLayout(
    val showTopBarInPane: Boolean,
    val showTopBarInScaffold: Boolean,
    val showBottomSheet: Boolean,
) {
    companion object {
        val Expanded = SelectorEpisodePaneLayout(
            showTopBarInPane = true,
            showTopBarInScaffold = false,
            showBottomSheet = false,
        )

        val Compact = SelectorEpisodePaneLayout(
            showTopBarInPane = false,
            showTopBarInScaffold = true,
            showBottomSheet = true,
        )

        fun calculate(
            scaffoldValue: ThreePaneScaffoldValue,
        ): SelectorEpisodePaneLayout {
            return when {
                scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded -> {
                    // list 和 extra 同时展开, 也就是大屏环境. list 内包含了配置, 所以我们无需使用 bottom sheet 显示配置
                    Expanded
                }

                else -> Compact
            }
        }
    }
}

