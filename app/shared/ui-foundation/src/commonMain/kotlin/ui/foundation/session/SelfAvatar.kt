/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.session

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.domain.session.AuthState
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.avatar.AvatarImage

@Composable
fun SelfAvatar(
    authState: AuthState,
    selfInfo: UserInfo?,
    modifier: Modifier = Modifier,
    handler: SelfAvatarActionHandler = rememberSelfAvatarActionHandler(),
    size: Dp = 48.dp,
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    Box {
        Surface({ showMenu = true }, modifier, shape = CircleShape) {
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

        DropdownMenu(
            showMenu,
            offset = DpOffset(x = 0.dp, y = 8.dp),
            onDismissRequest = { showMenu = false },
        ) {
            SelfAvatarMenus(handler, onClickAny = { showMenu = false })
        }
    }
}

@Stable
interface SelfAvatarActionHandler {
    fun onClickSettings()
}

private class DefaultSelfAvatarActionHandler(
    private val navigator: AniNavigator,
) : SelfAvatarActionHandler {
    override fun onClickSettings() {
        navigator.navigateSettings()
    }
}

@Composable
fun rememberSelfAvatarActionHandler(): SelfAvatarActionHandler {
    val navigator = LocalNavigator.current
    return remember(navigator) { DefaultSelfAvatarActionHandler(navigator) }
}

@Composable
private fun SelfAvatarMenus(
    handler: SelfAvatarActionHandler,
    onClickAny: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text("设置") },
        onClick = {
            handler.onClickSettings()
            onClickAny()
        },
        leadingIcon = { Icon(Icons.Rounded.Settings, null) },
    )
}
