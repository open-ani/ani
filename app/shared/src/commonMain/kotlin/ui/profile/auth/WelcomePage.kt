package me.him188.ani.app.ui.profile.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.icons.AniIcons
import me.him188.ani.app.ui.foundation.icons.GithubMark
import me.him188.ani.app.ui.foundation.icons.QqRoundedOutline
import me.him188.ani.app.ui.foundation.icons.Telegram
import me.him188.ani.app.ui.profile.AniHelpNavigator
import moe.tlaster.precompose.navigation.BackHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WelcomeViewModel : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()

    fun cancelRequest() {
        sessionManager.processingRequest.value?.cancel()
    }
}

@Composable
fun WelcomeScene(
    vm: WelcomeViewModel,
    modifier: Modifier = Modifier,
) {
    val navigator = LocalNavigator.current
    BackHandler {
        vm.cancelRequest()
        navigator.goBack()
    }
    WelcomePage(
        onClickLogin = { navigator.navigateBangumiOAuthOrTokenAuth() },
        onClickGuest = {
            vm.cancelRequest()
            navigator.navigateHome()
        },
        modifier = modifier,
    )
}

@Composable
fun WelcomePage(
    onClickLogin: () -> Unit,
    onClickGuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Scaffold(
            Modifier.widthIn(max = 600.dp),
            topBar = {
//            TopAppBar(title = { Text("欢迎") })
            },
            bottomBar = {
                Column(Modifier.navigationBarsPadding()) {
                    HorizontalDivider(Modifier.padding(horizontal = 4.dp))
                    Column(
                        Modifier.padding(all = 16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FilledTonalButton(onClickLogin, Modifier.fillMaxWidth()) {
                            Text("登录 / 注册 Bangumi")
                        }

                        Button(
                            onClick = onClickGuest,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("以游客身份免登录进入")
                        }
                        Text(
                            "游客可使用除追番进度管理外的所有功能", Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            },
        ) { contentPadding ->
            Column(
                Modifier.padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(all = 16.dp)
                    .widthIn(max = 1000.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("欢迎使用 Ani", style = MaterialTheme.typography.headlineMedium)

                ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                    Text("""Ani 是一个一站式在线弹幕追番平台。""")
                }

                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    Text("""Ani 目前由爱好者组成的组织 open-ani 和社区贡献者维护，在 GitHub 上开源。""")

                    Text("""Ani 启动于 2024 年 3 月，距离完善还有一段距离。欢迎加群或在 GitHub 反馈问题。""")
                }

                AniContactList()

                Text(
                    "资源来自于网络",
                    Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.titleLarge,
                )

                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    Text("""Ani 本身不提供资源保存和下载等服务。Ani 只会从互联网上搜索番剧资源，本质上为一个整合了进度管理、视频播放、弹幕、互联网资源搜索引擎等功能的工具。""")

                    Text("""Ani 也支持自建的 Jellyfin 等媒体服务。""")
                }

                Text(
                    """追番进度管理服务由 Bangumi 提供""",
                    Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.titleLarge,
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        Text("""Bangumi 番组计划 是一个中文 ACGN 互联网分享与交流项目，不提供资源下载。""")

                        Text(
                            """需要登录 Bangumi 账号方可使用收藏、记录观看进度等功能。""",
                        )
                    }
                }
//            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
//                Text("""· 注册时建议使用常见邮箱，例如 QQ, 网易, Outlook""")
//                Text("""· 如果提示激活失败，尝试删除激活码的最后一个字再手动输入""")
//                Text("""· 如果有其他问题，可加群获取帮助或在 GitHub 上提交 issue""")
//            }
            }
        }
    }
}

@Composable
fun AniContactList(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    FlowRow(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.Start),
    ) {
        SuggestionChip(
            { AniHelpNavigator.openGitHubHome(context) },
            icon = {
                Icon(AniIcons.GithubMark, null, Modifier.size(24.dp))
            },
            label = { Text("GitHub") },
        )

        SuggestionChip(
            { AniHelpNavigator.openAniWebsite(context) },
            icon = {
                Icon(
                    Icons.Rounded.Public, null,
                    Modifier.size(24.dp),
                )
            },
            label = { Text("官网") },
        )

        SuggestionChip(
            { AniHelpNavigator.openJoinQQGroup(context) },
            icon = {
                Icon(
                    AniIcons.QqRoundedOutline, null,
                    Modifier.size(24.dp),
                )
            },
            label = { Text("QQ 群") },
        )

        SuggestionChip(
            { AniHelpNavigator.openTelegram(context) },
            icon = {
                Image(
                    AniIcons.Telegram, null,
                    Modifier.size(24.dp),
                )
            },
            label = { Text("Telegram") },
        )
    }
}
