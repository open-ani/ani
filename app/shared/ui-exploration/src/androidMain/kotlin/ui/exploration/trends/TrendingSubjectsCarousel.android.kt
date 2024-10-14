/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

@file:OptIn(TestOnly::class)

package me.him188.ani.app.ui.exploration.trending

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.window.core.layout.WindowWidthSizeClass
import me.him188.ani.app.ui.foundation.ProvideFoundationCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.preview.PreviewSizeClasses
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.utils.platform.annotations.TestOnly

@Composable
@PreviewSizeClasses
@PreviewLightDark
private fun PreviewTrendingSubjectsCarousel() = ProvideFoundationCompositionLocalsForPreview {
    PreviewTrendingSubjectsCarouselImpl()
}

@Composable
private fun PreviewTrendingSubjectsCarouselImpl() {
    TrendingSubjectsCarousel(
        state = remember {
            TrendingSubjectsState(
                stateOf(TestTrendingSubjectInfos),
            )
        },
        {},
    )
}

@Composable
private fun checkWindowWidthSizeClass(expected: WindowWidthSizeClass) {
    check(currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == expected) {
        currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    }
}
