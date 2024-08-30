package me.him188.ani.app.platform.navigation

import me.him188.ani.app.navigation.AniNavigator

class SkikoOnBackPressedDispatcherOwner(
    private val aniNavigator: AniNavigator,
) : OnBackPressedDispatcherOwner {
    override val onBackPressedDispatcher: OnBackPressedDispatcher = OnBackPressedDispatcher(
        fallback = {
            aniNavigator.popBackStack()
        },
    )
}
