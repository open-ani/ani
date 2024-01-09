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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.home.LocalContentPaddings
import me.him188.ani.app.ui.subject.details.Avatar
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.openapitools.client.models.User

@Composable
fun ProfilePage() {
    val viewModel = remember { AuthViewModel() }

    Column(
        modifier = Modifier.padding(LocalContentPaddings.current).fillMaxSize(),
    ) {
        val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
        if (isProcessing) {
            Dialog(onDismissRequest = {}, DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
                Box(
                    Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        val authError by viewModel.authError.collectAsStateWithLifecycle()
        authError?.let { error ->
            Dialog(onDismissRequest = {
                viewModel.dismissError()
            }) {
                Text(error)
            }
        }

        val needAuth by viewModel.needAuth.collectAsStateWithLifecycle()
        if (needAuth) {
            key(viewModel.retryCount.value) {
                BangumiOAuthRequest(
                    onComplete = { viewModel.launchInBackground { setCode(it) } },
                    onFailed = { viewModel.onAuthFailed(it) },
                    Modifier.fillMaxSize()
                )
            }
        } else {
            // user profile
            UserProfileContent()
        }
    }
}

@Composable
private fun UserProfileContent() {
    val viewModel = remember { AccountViewModel() }

    val selfInfo by viewModel.selfInfo.collectAsStateWithLifecycle()
    Column {
        selfInfo?.let {
            SelfInfo(it)
        }
    }
}

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