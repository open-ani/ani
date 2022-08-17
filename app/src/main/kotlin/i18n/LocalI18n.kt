package me.him188.animationgarden.desktop.i18n

import androidx.compose.runtime.*
import androidx.compose.ui.text.intl.Locale


val LocalI18n: ProvidableCompositionLocal<ResourceBundle> = staticCompositionLocalOf {
    error("No ResourceBundle provided in current context")
}

@Composable
inline fun ProvideResourceBundleI18n(crossinline block: @Composable () -> Unit) {
    val currentBundle = remember(Locale.current.language) { ResourceBundle.load() }
    return CompositionLocalProvider(LocalI18n provides currentBundle) {
        block()
    }
}