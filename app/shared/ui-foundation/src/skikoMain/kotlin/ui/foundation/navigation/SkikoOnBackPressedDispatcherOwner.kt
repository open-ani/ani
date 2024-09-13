package me.him188.ani.app.ui.foundation.navigation

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
