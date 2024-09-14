/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import me.him188.ani.app.navigation.AniNavigator

/**
 * UI 入口点. 包含所有子页面, 以及组合这些子页面的方式 (navigation).
 */
@Composable
fun AniAppContent(aniNavigator: AniNavigator) {
    val navigator = rememberNavController()
    SideEffect {
        aniNavigator.setNavController(navigator)
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
//            if (isLandscape) {
//                AniAppContentLandscape(aniNavigator, Modifier.fillMaxSize())
//            } else {
        AniAppContentPortrait(aniNavigator, Modifier.fillMaxSize())
//            }
    }
}
