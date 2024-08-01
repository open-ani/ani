package me.him188.ani.app.platform

import androidx.compose.runtime.Stable

@Stable
actual val currentAniBuildConfigImpl: AniBuildConfig
    get() = AniBuildConfigIos

