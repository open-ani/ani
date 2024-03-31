package me.him188.ani.app.tools

val LocalTimeFormatter = androidx.compose.runtime.compositionLocalOf<TimeFormatter> {
    error("No TimeFormatter provided")
}
