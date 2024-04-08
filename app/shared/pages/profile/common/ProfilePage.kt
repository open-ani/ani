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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.isLoggedIn
import me.him188.ani.app.ui.main.LocalContentPaddings
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.openapitools.client.models.User

@Composable
fun ProfilePage(modifier: Modifier = Modifier) {
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
                Modifier.fillMaxWidth()
            )

            // debug
            DebugInfoView(viewModel, Modifier.padding(horizontal = 16.dp))
        }
    }
}

private const val GITHUB_REPO = "https://github.com/him188/ani"
private const val ISSUE_TRACKER = "https://github.com/him188/ani/issues"
private const val RELEASES = "https://github.com/him188/ani/releases"
private const val BANGUMI = "https://bangumi.tv"
private const val DANDANPLAY = "https://www.dandanplay.com/"
private const val DMHY = "https://dmhy.b168.net/"
private const val ACG_RIP = "https://acg.rip/"

@Composable
@OptIn(DelicateCoroutinesApi::class)
private fun DebugInfoView(viewModel: AccountViewModel, modifier: Modifier = Modifier) {
    val vm = rememberViewModel<DebugInfoViewModel> { DebugInfoViewModel() }
    val debugInfo by vm.debugInfo.collectAsStateWithLifecycle(null)
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }

    var latestVersion: NewVersion? by remember { mutableStateOf(null) }

    LaunchedEffect(true) {
        latestVersion = withContext(vm.backgroundScope.coroutineContext) {
            vm.getLatestVersionOrNull()
        }
    }

    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(Modifier.height(16.dp))

        val context by rememberUpdatedState(LocalContext.current)

        when {
            latestVersion == null -> {
                Text(
                    "当前版本为 ${vm.currentVersion}, 正在检查更新...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            latestVersion?.name == vm.currentVersion -> {
                Row {
                    Text(
                        "当前版本为 ${vm.currentVersion}, 已经是最新了",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                Text(
                    "当前版本为 ${vm.currentVersion}, 有更新, 最新版本为 ${latestVersion?.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    "发布时间: " + latestVersion?.publishedAt.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    "更新内容: ",
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    latestVersion?.changelog.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Row(Modifier.align(Alignment.End)) {
                    TextButton({ vm.browserNavigator.openBrowser(context, RELEASES) }) {
                        Text("在 GitHub 上查看")
                    }
                    Button({ vm.browserNavigator.openBrowser(context, latestVersion!!.apkUrl) }) {
                        Text("下载更新")
                    }
                }
            }
        }

        HorizontalDivider()

        Text(
            "感谢你参加测试",
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            buildAnnotatedString {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append("你正在使用测试版本的 Ani. ")
                pop()
                append("Ani 目前主要在优化播放体验, 还有很多其他想法没实现. 如果你在测试过程中遇到问题或有任何建议, 欢迎反馈到 GitHub issues!")
            },
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton({ clipboard.setText(AnnotatedString(ISSUE_TRACKER)) }) {
                Text("复制问题反馈网址")
            }
            Button({ vm.browserNavigator.openBrowser(context, ISSUE_TRACKER) }) {
                Text("打开问题反馈页面")
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
            "鸣谢",
            style = MaterialTheme.typography.titleMedium,
        )

        ClickableText(
            buildAnnotatedString {
                append("Ani 完全免费无广告且开源, 源代码可在 ")
                pushStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                )
                append("GitHub")
                pop()
                append(" 找到")
            },
            style = MaterialTheme.typography.bodyMedium,
        ) {
            vm.browserNavigator.openBrowser(context, GITHUB_REPO)
        }

        Text("Ani 使用了许多爱好者用爱发电维护的免费服务.", style = MaterialTheme.typography.bodyMedium)

        Text("特别感谢:", style = MaterialTheme.typography.bodyMedium)

        val listStyle = MaterialTheme.typography.bodyMedium.copy(MaterialTheme.colorScheme.primary)
        ClickableText(
            AnnotatedString("· Bangumi 番组计划"),
            style = listStyle,
        ) {
            vm.browserNavigator.openBrowser(context, BANGUMI)
        }

        ClickableText(
            AnnotatedString("· 弹弹play"),
            style = listStyle,
        ) {
            vm.browserNavigator.openBrowser(context, DANDANPLAY)
        }

        ClickableText(
            AnnotatedString("· 动漫花园资源网"),
            style = listStyle,
        ) {
            vm.browserNavigator.openBrowser(context, DMHY)
        }

        ClickableText(
            AnnotatedString("· acg.rip"),
            style = listStyle,
        ) {
            vm.browserNavigator.openBrowser(context, ACG_RIP)
        }

        ClickableText(
            AnnotatedString("· 所有字幕组"),
            style = MaterialTheme.typography.bodyMedium,
        ) {}

        ClickableText(
            AnnotatedString("· 你"),
            style = MaterialTheme.typography.bodyMedium,
        ) {}

        HorizontalDivider()

        Text(
            "调试信息",
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            "以下为测试版本的调试信息, 如遇到登录失败等登录相关问题请截图本页面并一起提交给开发者",
            style = MaterialTheme.typography.labelMedium,
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for ((name, value) in debugInfo?.properties.orEmpty()) {
                Text(
                    "$name: $value",
                    Modifier.fillMaxWidth().clickable {
                        value?.let { clipboard.setText(AnnotatedString(it)) }
                        GlobalScope.launch {
                            snackbar.showSnackbar("Copied")
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        FilledTonalButton({ viewModel.logout() }, enabled = viewModel.logoutEnabled) {
            Text("Log out")
        }

        PlatformDebugInfoItems(viewModel, snackbar)

        Spacer(Modifier.height(16.dp))

        HorizontalDivider()

        Text(
            "提示: 此页面原本设计为展示\"我\"的近期动态等信息, 测试阶段临时用来展示调试信息",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic,
            ),
        )
    }

    SnackbarHost(snackbar)
}

@Composable
internal expect fun ColumnScope.PlatformDebugInfoItems(viewModel: AccountViewModel, snackbar: SnackbarHostState)

@Composable
internal fun SelfInfo(selfInfo: User?, isLoggedIn: Boolean?, modifier: Modifier = Modifier) {
    Box {
        UserInfoRow(selfInfo, {}, modifier)

        if (isLoggedIn == false) {
            Surface(Modifier.matchParentSize()) {
                UnauthorizedTips(Modifier.fillMaxSize())
            }
        }
    }
}


@Preview
@Composable
private fun PreviewSelfInfoCommon() {
    ProvideCompositionLocalsForPreview {
        SelfInfo(null, false)
    }
}


@Preview
@Composable
private fun PreviewProfilePage() {
    ProvideCompositionLocalsForPreview {
        ProfilePage()
    }
}