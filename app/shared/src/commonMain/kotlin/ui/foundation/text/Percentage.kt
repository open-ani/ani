package me.him188.ani.app.ui.foundation.text

import androidx.compose.runtime.Stable

@Stable
fun Float.toPercentageString(): String {
    return "${(this * 100).toInt()}%"
}
