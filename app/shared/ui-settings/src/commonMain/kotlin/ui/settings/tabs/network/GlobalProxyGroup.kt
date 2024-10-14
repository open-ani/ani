/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import me.him188.ani.app.data.models.preference.ProxyConfigRegardlessOfEnabled
import me.him188.ani.app.data.models.preference.ProxySettings
import me.him188.ani.app.ui.settings.framework.SettingsState
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextFieldItem
import me.him188.ani.utils.ktor.ClientProxyConfigValidator


@OptIn(ProxyConfigRegardlessOfEnabled::class)
@Composable
internal fun SettingsScope.GlobalProxyGroup(
    proxySettingsState: SettingsState<ProxySettings>,
) {
    val proxySettings: ProxySettings by proxySettingsState
    Group(
        title = { Text("全局代理设置") },
        description = {
            Text("应用于所有数据源以及 Bangumi")
        },
    ) {
        SwitchItem(
            checked = proxySettings.default.enabled,
            onCheckedChange = {
                proxySettingsState.update(proxySettings.copy(default = proxySettings.default.copy(enabled = it)))
            },
            title = { Text("启用代理") },
            description = { Text("启用后下面的配置才生效") },
        )

        HorizontalDividerItem()

        TextFieldItem(
            proxySettings.default.config.url,
            title = { Text("代理地址") },
            description = {
                Text(
                    "示例: http://127.0.0.1:7890 或 socks5://127.0.0.1:1080",
                )
            },
            onValueChangeCompleted = {
                proxySettingsState.update(
                    proxySettings.copy(
                        default = proxySettings.default.copy(
                            config = proxySettings.default.config.copy(
                                url = it,
                            ),
                        ),
                    ),
                )
            },
            isErrorProvider = {
                !ClientProxyConfigValidator.isValidProxy(it)
            },
            sanitizeValue = { it.trim() },
        )

        HorizontalDividerItem()

        val username by remember {
            derivedStateOf {
                proxySettings.default.config.authorization?.username ?: ""
            }
        }

        val password by remember {
            derivedStateOf {
                proxySettings.default.config.authorization?.password ?: ""
            }
        }

        TextFieldItem(
            username,
            title = { Text("用户名") },
            description = { Text("可选") },
            placeholder = { Text("无") },
            onValueChangeCompleted = {
                proxySettingsState.update(
                    proxySettings.copy(
                        default = proxySettings.default.copy(
                            config = proxySettings.default.config.copy(
                                authorization = proxySettings.default.config.authorization?.copy(
                                    username = it,
                                ),
                            ),
                        ),
                    ),
                )
            },
            sanitizeValue = { it },
        )

        HorizontalDividerItem()

        TextFieldItem(
            password,
            title = { Text("密码") },
            description = { Text("可选") },
            placeholder = { Text("无") },
            onValueChangeCompleted = {
                proxySettingsState.update(
                    proxySettings.copy(
                        default = proxySettings.default.copy(
                            config = proxySettings.default.config.copy(
                                authorization = proxySettings.default.config.authorization?.copy(
                                    password = password,
                                ),
                            ),
                        ),
                    ),
                )
            },
            sanitizeValue = { it },
        )
    }
}

