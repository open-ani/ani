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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.ui.main.LocalContentPaddings
import me.him188.ani.app.ui.subject.details.Avatar
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.openapitools.client.models.User

@Composable
fun ProfilePage() {
    val navigator = LocalNavigator.current
    LaunchedEffect(true) {
        navigator.requestBangumiAuthorization()
    }
    val viewModel = remember { AccountViewModel() }
    Column(
        modifier = Modifier.padding(LocalContentPaddings.current).fillMaxSize(),
    ) {
        // user profile
        val selfInfo by viewModel.selfInfo.collectAsStateWithLifecycle()
        Column {
            selfInfo?.let {
                SelfInfo(it)
            }

            // debug
            if (currentAniBuildConfig.isDebug) {
                DebugInfoView(viewModel)
            }
        }
    }
}

@Composable
@OptIn(DelicateCoroutinesApi::class)
private fun DebugInfoView(viewModel: AccountViewModel) {
    val debugInfo by viewModel.debugInfo.collectAsStateWithLifecycle(null)
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }

    Column(Modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for ((name, value) in debugInfo?.properties.orEmpty()) {
            TextButton(
                onClick = {
                    value?.let { clipboard.setText(AnnotatedString(it)) }
                    GlobalScope.launch {
                        snackbar.showSnackbar("Copied")
                    }
                },
                Modifier.fillMaxWidth()
            ) {
                Text("$name: $value")
            }
        }

        PlatformDebugInfoItems(viewModel, snackbar)
    }

    SnackbarHost(snackbar)
}

@Composable
internal expect fun ColumnScope.PlatformDebugInfoItems(viewModel: AccountViewModel, snackbar: SnackbarHostState)

@Composable
private fun SelfInfo(selfInfo: User) {
    Row(Modifier.fillMaxWidth().padding(16.dp)) {
        Box(Modifier.size(64.dp).clip(CircleShape)) {
            Avatar(selfInfo.avatar.medium, Modifier.matchParentSize())
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Row {
                Text(selfInfo.nickname, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
//                Text(selfInfo.username, style = MaterialTheme)
            }
            Text(selfInfo.sign, style = MaterialTheme.typography.bodySmall)
        }
    }
}