package me.him188.ani.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CompletableDeferred
import me.him188.ani.app.ui.settings.SettingsTab
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo
import org.koin.core.component.KoinComponent
import org.koin.mp.KoinPlatform

/**
 * Supports navigation to any page in the app.
 *
 * @see LocalNavigator
 */
interface AniNavigator {
    fun setNavigator(
        navigator: Navigator,
    )

    suspend fun awaitNavigator(): Navigator

    val navigator: Navigator

    fun goBack() {
        navigator.goBack()
    }

    fun popUntilNotAuth() {
        navigator.goBack(PopUpTo("/bangumi-token-oauth", inclusive = true), inclusive = true)
        navigator.goBack(PopUpTo("/bangumi-oauth", inclusive = true), inclusive = true)
    }

    fun navigateSubjectDetails(subjectId: Int) {
        navigator.navigate("/subjects/$subjectId")
    }

    fun navigateSubjectCaches(subjectId: Int) {
        navigator.navigate("/subjects/$subjectId/caches")
    }

    fun navigateEpisodeDetails(subjectId: Int, episodeId: Int, fullscreen: Boolean = false) {
        navigator.navigate(
            "/subjects/$subjectId/episodes/$episodeId?fullscreen=$fullscreen",
            NavOptions(launchSingleTop = true, includePath = true),
        )
    }

    fun navigateWelcome() {
        navigator.navigate("/welcome")
    }

    fun navigateHome() {
        navigator.navigate("/home")
    }

    fun navigateSearch(requestFocus: Boolean = false) {
        navigator.navigate("/home?tab=search")
    }

    /**
     * 登录页面
     */
    fun navigateBangumiOAuthOrTokenAuth() {
        navigator.navigate("/bangumi-oauth", NavOptions(launchSingleTop = true))
    }

    fun navigateBangumiTokenAuth() {
        navigator.navigate(
            "/bangumi-token-auth",
            NavOptions(
                launchSingleTop = true,
                popUpTo = PopUpTo("/bangumi-oauth", inclusive = true),
            ),
        )
    }

    fun navigateSettings(tab: SettingsTab = SettingsTab.Default) {
        navigator.navigate("/settings?tab=${tab.ordinal}&back=true")
    }

    fun navigateCaches() {
        navigator.navigate("/caches")
    }

    fun navigateCacheDetails(cacheId: String) {
        navigator.navigate("/caches/$cacheId")
    }
}

fun AniNavigator(): AniNavigator = AniNavigatorImpl()

private class AniNavigatorImpl : AniNavigator, KoinComponent {
    private val _navigator: CompletableDeferred<Navigator> = CompletableDeferred()

    override val navigator: Navigator
        get() = _navigator.getCompleted()

    override fun setNavigator(navigator: Navigator) {
        this._navigator.complete(navigator)
    }

    override suspend fun awaitNavigator(): Navigator {
        return _navigator.await()
    }
}

/**
 * It is always provided.
 */
val LocalNavigator = compositionLocalOf<AniNavigator> {
    error("Navigator not found")
}

// dummy
object LocalBrowserNavigator {
    @Stable
    val current get() = KoinPlatform.getKoin().get<BrowserNavigator>()
}

@Composable
inline fun OverrideNavigation(
    noinline newNavigator: @DisallowComposableCalls (AniNavigator) -> AniNavigator,
    crossinline content: @Composable () -> Unit
) {
    val current by rememberUpdatedState(LocalNavigator.current)
    val newNavigatorUpdated by rememberUpdatedState(newNavigator)
    val new by remember {
        derivedStateOf {
            newNavigatorUpdated(current)
        }
    }
    CompositionLocalProvider(LocalNavigator provides new) {
        content()
    }
}
