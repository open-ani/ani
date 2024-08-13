package me.him188.ani.app.ui.subject.collection.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.SyncProblem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.source.session.AuthState
import me.him188.ani.app.data.source.session.SessionStatus
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.foundation.layout.paddingIfNotEmpty
import me.him188.ani.app.ui.foundation.text.ProvideContentColor

// 留着以后可能要改成支持其他错误类型

//
//@Immutable
//sealed class SessionTipsKind {
//    /**
//     * 正在登录或者正在加载数据
//     */
//    data object Loading : SessionTipsKind()
//
//    /**
//     * 该操作需要登录
//     */
//    data class AuthorizationRequired(
//        /**
//         * 之前是否登录过.
//         * 为 `true` 时意味着之前登录过的会话过期了.
//         * 为 `false` 时意味着没有保存的会话信息.
//         */
//        val wasLoggedIn: Boolean,
//    ) : SessionTipsKind()
//
//    /**
//     * 操作成功
//     */
//    data object Success : SessionTipsKind()
//
//    /**
//     * 客户端网络错误
//     */
//    data object NetworkError : SessionTipsKind()
//
//    /**
//     * 服务器响应了 500
//     */
//    data object ServiceUnavailable : SessionTipsKind()
//}
//
//fun State<SessionStatus>.toSessionTipKind() =
//    when (value) {
//        is SessionStatus.Verified -> SessionTipsKind.Success
//        is SessionStatus.Verifying -> SessionTipsKind.Loading
//        SessionStatus.Refreshing -> SessionTipsKind.Loading
//        SessionStatus.NoToken -> SessionTipsKind.AuthorizationRequired(wasLoggedIn = false)
//        SessionStatus.Expired -> SessionTipsKind.AuthorizationRequired(wasLoggedIn = true)
//        SessionStatus.NetworkError -> SessionTipsKind.NetworkError
//        SessionStatus.ServiceUnavailable -> SessionTipsKind.ServiceUnavailable
//    }

/**
 * @param guest
 */
@Composable
fun SessionTipsArea(
    authState: AuthState,
    guest: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigator = LocalNavigator.current
    SessionTipsArea(
        authState.status ?: SessionStatus.Refreshing,
        onLogin = {
            authState.launchAuthorize(navigator)
        },
        onRetry = {
            authState.retry()
        },
        guest = guest,
        modifier,
    )
}

/**
 * 用于显示未登录时的提示和相关动作按钮的区域.
 *
 * 占用两三行的高度, 包含两个按钮, 一个用于登录, 一个用于搜索.
 *
 * @param guest
 */
@Composable
fun SessionTipsArea(
    status: SessionStatus,
    onLogin: () -> Unit,
    onRetry: () -> Unit,
    guest: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.paddingIfNotEmpty(horizontal = 16.dp).fillMaxWidth().widthIn(max = 400.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (status) {
            is SessionStatus.Verified -> {

            }

            is SessionStatus.Verifying, SessionStatus.Refreshing -> {
                CircularProgressIndicator()
            }

            SessionStatus.NoToken -> guest()
            SessionStatus.Expired -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.HowToReg, null)
                    Text("登录过期，请重新登录")
                }
                FilledTonalButton(onLogin, Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Rounded.Login, null)
                    Text("登录")
                }
            }

            SessionStatus.NetworkError -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.CloudOff, null)
                    Text("网络错误，请检查网络连接")
                }
                RetryButton(onRetry)
            }

            SessionStatus.ServiceUnavailable -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.SyncProblem, null)
                    Text("服务异常，请稍后再试")
                }
                RetryButton(onRetry)
            }

            SessionStatus.Guest -> TextButton(onLogin) {
                Text("游客模式，点击登录")
            }
        }
    }
}

@Composable
private fun RetryButton(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(onRetry, modifier.fillMaxWidth()) {
        Icon(Icons.Rounded.Sync, null)
        Text("重试", Modifier.padding(start = 8.dp))
    }
}

@Composable
fun SessionTipsIcon(
    authState: AuthState,
    modifier: Modifier = Modifier,
    showLoading: Boolean = true,
    showLabel: Boolean = true,
) {
    val navigator = LocalNavigator.current
    SessionTipsIcon(
        authState.status ?: SessionStatus.Refreshing,
        onLogin = {
            authState.launchAuthorize(navigator)
        },
        onRetry = {
            authState.retry()
        },
        modifier,
        showLoading = showLoading,
        showLabel = showLabel,
    )
}

@Stable
private val NO_ACTION = {}

@Composable
fun SessionTipsIcon(
    status: SessionStatus,
    onLogin: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    showLoading: Boolean = true,
    showLabel: Boolean = true,
) {
    val action = when (status) {
        SessionStatus.Guest -> onLogin
        is SessionStatus.Verified -> NO_ACTION
        is SessionStatus.Loading -> NO_ACTION
        SessionStatus.Expired -> onLogin
        SessionStatus.NetworkError -> onRetry
        SessionStatus.NoToken -> onLogin
        SessionStatus.ServiceUnavailable -> onRetry
    }
    TextButton(
        action,
        modifier.animateContentSize(),
        enabled = status !is SessionStatus.Verified,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (status) {
                is SessionStatus.Verified -> {}

                is SessionStatus.Loading -> {
                    if (showLoading) {
                        var rotation by remember { mutableStateOf(0f) }
                        LaunchedEffect(true) {
                            animate(
                                360f,
                                0f,
                                animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
                            ) { value, _ ->
                                rotation = value
                            }
                        }
                        Box(
                            Modifier.graphicsLayer {
                                rotationZ = rotation
                            },
                        ) {
                            Icon(Icons.Rounded.Sync, "正在刷新", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                SessionStatus.NoToken -> {
                    ProvideContentColor(MaterialTheme.colorScheme.primary) {
                        Icon(Icons.Rounded.HowToReg, "登录")
                        Text("登录")
                    }
                }

                SessionStatus.Expired -> {
                    ProvideContentColor(MaterialTheme.colorScheme.error) {
                        Icon(
                            Icons.Rounded.SyncProblem,
                            "登录过期",
                        )
                        if (showLabel) {
                            Text("登录过期")
                        }
                    }
                }

                SessionStatus.NetworkError -> {
                    ProvideContentColor(MaterialTheme.colorScheme.error) {
                        Icon(
                            Icons.Rounded.SyncProblem,
                            "网络错误",
                            tint = MaterialTheme.colorScheme.error,
                        )
                        if (showLabel) {
                            Text("网络错误")
                        }
                    }
                }

                SessionStatus.ServiceUnavailable -> {
                    ProvideContentColor(MaterialTheme.colorScheme.error) {
                        Icon(
                            Icons.Rounded.SyncProblem,
                            "服务器异常",
                            tint = MaterialTheme.colorScheme.error,
                        )
                        if (showLabel) {
                            Text("服务异常")
                        }
                    }
                }

                SessionStatus.Guest -> {
                    if (showLabel) {
                        Text("游客模式")
                    }
                }
            }

        }
    }
}
