package me.him188.ani.app.ui.foundation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf

val LocalSnackbar = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}