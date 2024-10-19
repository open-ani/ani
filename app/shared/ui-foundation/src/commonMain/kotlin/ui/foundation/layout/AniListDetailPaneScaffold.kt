/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.navigation.BackHandler


/**
 * 自动适应单页模式和双页模式的布局的 paddings
 */
@Composable
fun <T> AniListDetailPaneScaffold(
    navigator: ThreePaneScaffoldNavigator<T>,
    listPaneTopAppBar: @Composable () -> Unit,
    listPaneContent: @Composable (PaneScope.() -> Unit),
    detailPane: @Composable (PaneScope.() -> Unit),
    modifier: Modifier = Modifier,
    listPanePreferredWidth: Dp = Dp.Unspecified,
    layoutParameters: ListDetailLayoutParameters = ListDetailLayoutParameters.calculate(navigator.scaffoldDirective),
) {
    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }
    val layoutParametersState by rememberUpdatedState(layoutParameters)
    ListDetailPaneScaffold(
        navigator.scaffoldDirective,
        navigator.scaffoldValue,
        listPane = {
            val threePaneScaffoldScope = this
            AnimatedPane1(Modifier.preferredWidth(listPanePreferredWidth)) {
                Column {
                    listPaneTopAppBar()
                    val scope = remember(threePaneScaffoldScope) {
                        object : PaneScope {
                            override val listDetailLayoutParameters: ListDetailLayoutParameters
                                get() = layoutParametersState

                            override fun Modifier.paneContentPadding(): Modifier =
                                Modifier.padding(layoutParametersState.listPaneContentPaddingValues)
                        }
                    }
                    listPaneContent(scope)
                }
            }
        },
        detailPane = {
            val threePaneScaffoldScope = this
            AnimatedPane1 {
                Card(
                    shape = layoutParameters.detailPaneShape,
                    colors = layoutParameters.detailPaneColors,
                ) {
                    val scope = remember(threePaneScaffoldScope) {
                        object : PaneScope {
                            override val listDetailLayoutParameters: ListDetailLayoutParameters
                                get() = layoutParametersState

                            override fun Modifier.paneContentPadding(): Modifier =
                                Modifier.padding(layoutParametersState.detailPaneContentPaddingValues)
                        }
                    }
                    detailPane(scope)
                }
            }
        },
        modifier,
    )
}

@Stable
interface PaneScope {
    val listDetailLayoutParameters: ListDetailLayoutParameters

    /**
     * 增加自动的 content padding
     */
    @Stable
    fun Modifier.paneContentPadding(): Modifier
}

@Immutable
data class ListDetailLayoutParameters(
    val listPaneContentPaddingValues: PaddingValues,
    val detailPaneContentPaddingValues: PaddingValues,
    val detailPaneShape: Shape,
    val detailPaneColors: CardColors,
    val isSinglePane: Boolean,
) {
    companion object {
        @Composable
        fun calculate(directive: PaneScaffoldDirective): ListDetailLayoutParameters {
            val isTwoPane = directive.maxHorizontalPartitions > 1
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
            return if (isTwoPane) {
                ListDetailLayoutParameters(
                    listPaneContentPaddingValues = PaddingValues(
                        start = windowSizeClass.paneHorizontalPadding,
                        end = 0.dp, // ListDetail 两个 pane 之间自带 24.dp
                    ),
                    detailPaneContentPaddingValues = PaddingValues(0.dp),
                    detailPaneShape = MaterialTheme.shapes.extraLarge.copy(
                        topEnd = ZeroCornerSize,
                        bottomEnd = ZeroCornerSize,
                    ),
                    detailPaneColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    isSinglePane = false,
                )
            } else {
                ListDetailLayoutParameters(
                    listPaneContentPaddingValues = PaddingValues(horizontal = windowSizeClass.paneHorizontalPadding),
                    detailPaneContentPaddingValues = PaddingValues(horizontal = windowSizeClass.paneHorizontalPadding),
                    detailPaneShape = RectangleShape,
                    detailPaneColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    isSinglePane = true,
                )
            }
        }
    }
}
