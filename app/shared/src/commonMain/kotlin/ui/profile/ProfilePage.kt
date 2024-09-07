/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.source.session.AuthState
import me.him188.ani.app.data.source.session.userInfoOrNull
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import org.koin.mp.KoinPlatform

@Composable
fun ProfilePage(
    onClickSettings: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    val viewModel = remember { AccountViewModel() }

    // user profile
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SelfInfo(
                viewModel.authState,
                viewModel.authState.isKnownLoggedOut || viewModel.authState.isKnownGuest,
                onClickSettings,
                modifier = Modifier
                    .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                    .fillMaxWidth(),
            )
        },
    ) { topBarPaddings ->
        // debug
        DebugInfoView(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(
                    top = topBarPaddings.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding(),
                ),
        )
    }

}

internal const val GITHUB_HOME = "https://github.com/open-ani/ani"
internal const val ANI_WEBSITE = "https://myani.org"
internal const val ISSUE_TRACKER = "https://github.com/open-ani/ani/issues"

@Composable
private fun DebugInfoView(modifier: Modifier = Modifier) {
    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "感谢你的支持",
            Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.titleMedium,
        )

        AniHelpSection(Modifier)
    }
}

@Composable
fun AniHelpSection(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val context by rememberUpdatedState(LocalContext.current)

        Text(
            "欢迎加入 QQ 群反馈建议或者闲聊: 927170241. Telegram 群 openani. 如遇到问题, 除加群外也可以在 GitHub 反馈.",
        )

        Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box {
                var showOpenDropdown by remember { mutableStateOf(false) }
                DropdownMenu(showOpenDropdown, { showOpenDropdown = false }) {
                    DropdownMenuItem(
                        text = { Text("GitHub") },
                        onClick = {
                            KoinPlatform.getKoin().get<BrowserNavigator>().openBrowser(context, GITHUB_HOME)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("反馈问题") },
                        onClick = { KoinPlatform.getKoin().get<BrowserNavigator>().openBrowser(context, ISSUE_TRACKER) },
                    )
                    DropdownMenuItem(
                        text = { Text("Ani 官网") },
                        onClick = { KoinPlatform.getKoin().get<BrowserNavigator>().openBrowser(context, ANI_WEBSITE) },
                    )
                }

                OutlinedButton({ showOpenDropdown = true }) {
                    Text("打开...")
                }
            }

            Box {
                var showHelp by remember { mutableStateOf(false) }
                Button({ showHelp = true }) {
                    Text("加群")
                }
                HelpDropdown(showHelp, { showHelp = false })
            }
        }

        Text(
            "要让每个番剧都拥有不错的弹幕量需要不小用户基数, 如果你喜欢本应用, 请向朋友推荐以增加弹幕量!",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
internal fun SelfInfo(
    authState: AuthState,
    isLoggedOut: Boolean,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
    ) {
        UserInfoRow(
            authState.status?.userInfoOrNull,
            onClickEditNickname = {},
            onClickSettings = onClickSettings,
            modifier,
        )

        if (isLoggedOut) {
            Surface(Modifier.align(Alignment.Center)) {
                val navigator = LocalNavigator.current
                Box(Modifier.padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    OutlinedButton({ authState.launchAuthorize(navigator) }) {
                        Text("请先登录")
                    }
                }
            }
        }
    }
}
