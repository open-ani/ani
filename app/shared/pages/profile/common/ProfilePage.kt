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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.isLoggedIn
import me.him188.ani.app.ui.main.LocalContentPaddings
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.core.context.GlobalContext
import org.openapitools.client.models.User

@Composable
fun ProfilePage(
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { AccountViewModel() }
    Column(
        modifier = modifier
            .systemBarsPadding()
            .padding(LocalContentPaddings.current).fillMaxSize(),
    ) {
        // user profile
        val selfInfo by viewModel.selfInfo.collectAsStateWithLifecycle()
        val loggedIn by isLoggedIn()
        Column {
            SelfInfo(
                selfInfo,
                loggedIn,
                onClickSettings,
                Modifier.fillMaxWidth(),
            )

            // debug
            DebugInfoView(Modifier.padding(horizontal = 16.dp))
        }
    }
}

private const val ISSUE_TRACKER = "https://github.com/him188/ani/issues"

@Composable
private fun DebugInfoView(modifier: Modifier = Modifier) {
    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(Modifier.height(16.dp))

        val context by rememberUpdatedState(LocalContext.current)

        Text(
            "感谢你参加测试",
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            "Ani 目前主要在优化播放体验, 还有很多其他想法没实现. 欢迎加入 QQ 群反馈建议或者闲聊: 927170241. 如遇到问题, 除加群外也可以在 GitHub 反馈."
        )

        Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ GlobalContext.get().get<BrowserNavigator>().openBrowser(context, ISSUE_TRACKER) }) {
                Text("打开 GitHub")
            }

            Button({ GlobalContext.get().get<BrowserNavigator>().openJoinGroup(context) }) {
                Text("加群")
            }
        }

        Text(
            "要让每个番剧都拥有不错的弹幕量需要不小用户基数, 如果你喜欢本应用, 请向朋友推荐以增加弹幕量!",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
        )

        HorizontalDivider()

        Text(
            "之前的东西哪去了? -- 鸣谢搬到设置里了; 检查更新现在是自动的, 有更新时你就知道了",
            style = MaterialTheme.typography.labelMedium,
            fontStyle = FontStyle.Italic
        )
        Text(
            "这是限时提示, 只有测试用户可以看到!",
            style = MaterialTheme.typography.labelSmall,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
internal fun SelfInfo(
    selfInfo: User?,
    isLoggedIn: Boolean?,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box {
        UserInfoRow(
            selfInfo,
            onClickEditNickname = {},
            onClickSettings = onClickSettings,
            modifier
        )

        if (isLoggedIn == false) {
            Surface(Modifier.matchParentSize()) {
                UnauthorizedTips(Modifier.fillMaxSize())
            }
        }
    }
}
