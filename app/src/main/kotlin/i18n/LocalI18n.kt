package me.him188.animationgarden.desktop.i18n

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf


@Stable
val LocalI18n: ProvidableCompositionLocal<ResourceBundle> = staticCompositionLocalOf {
    error("No ResourceBundle provided in current context")
}
