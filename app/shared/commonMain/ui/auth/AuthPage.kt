package me.him188.ani.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.profile.AuthViewModel
import me.him188.ani.app.ui.profile.BangumiOAuthRequest
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle


@Composable
fun AuthPage(
    viewModel: AuthViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnComplete by rememberUpdatedState(onComplete)
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
        if (isProcessing) {
            Dialog(onDismissRequest = {}, DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
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
                    onComplete = {
                        viewModel.launchInBackground {
                            setCode(it)
                            withContext(Dispatchers.Main) { currentOnComplete() }
                        }
                    },
                    onFailed = {
                        viewModel.onAuthFailed(it)
                    },
                    Modifier.fillMaxSize()
                )
            }
        } else {
            // already logged in
        }
    }
}
