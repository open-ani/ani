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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
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
        Column {
            SelfInfo(
                selfInfo,
                viewModel.isLoggedIn.collectAsStateWithLifecycle().value,
                Modifier.fillMaxWidth()
            )

            // debug
            if (currentAniBuildConfig.isDebug) {
                DebugInfoView(viewModel, Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
@OptIn(DelicateCoroutinesApi::class)
private fun DebugInfoView(viewModel: AccountViewModel, modifier: Modifier = Modifier) {
    val debugInfo by viewModel.debugInfo.collectAsStateWithLifecycle(null)
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Debug Tools", style = MaterialTheme.typography.titleMedium)

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

        Button({ viewModel.logout() }, enabled = viewModel.logoutEnabled) {
            Text("Log out")
        }

        PlatformDebugInfoItems(viewModel, snackbar)
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
private fun PreviewSelfInfo() {
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