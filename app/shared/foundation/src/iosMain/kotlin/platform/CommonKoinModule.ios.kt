package me.him188.ani.app.platform

import androidx.compose.runtime.Stable

@PublishedApi
@Stable
internal actual val currentAniBuildConfigImpl: AniBuildConfig
    get() = AniBuildConfigIos

