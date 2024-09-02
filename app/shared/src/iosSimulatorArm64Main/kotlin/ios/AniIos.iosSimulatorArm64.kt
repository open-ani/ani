package me.him188.ani.app.ios

import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.platform.navigation.OnBackPressedDispatcherOwner

actual fun OnBackPressedDispatcherOwner(aniNavigator: AniNavigator): OnBackPressedDispatcherOwner {
    return me.him188.ani.app.platform.navigation.SkikoOnBackPressedDispatcherOwner(aniNavigator)
}

