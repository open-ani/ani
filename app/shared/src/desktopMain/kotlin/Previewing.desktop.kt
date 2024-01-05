package me.him188.animationgarden.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import me.him188.animationgarden.app.platform.ContextImpl
import me.him188.animationgarden.app.platform.LocalContext
import java.io.File

@Composable
actual fun PlatformPreviewCompositionLocalProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalContext provides ContextImpl(File("."))) {
        content()
    }
}