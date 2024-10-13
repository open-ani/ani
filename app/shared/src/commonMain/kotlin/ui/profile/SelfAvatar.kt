/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.domain.session.AuthState
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.subject.collection.components.SessionTipsIcon

@Composable
fun SelfAvatar(
    authState: AuthState,
    selfInfo: UserInfo?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Box(modifier) {
        if (authState.isLoading || authState.isKnownLoggedIn) {
            // 加载中时展示 placeholder
            AvatarImage(
                url = selfInfo?.avatarUrl,
                Modifier.size(size).clip(CircleShape).placeholder(selfInfo == null),
            )
        } else {
            if (authState.isKnownGuest) {
                val navigator = LocalNavigator.current
                TextButton({ authState.launchAuthorize(navigator) }) {
                    Text("登录")
                }
            } else {
                SessionTipsIcon(authState, showLabel = false)
            }
        }
    }
}