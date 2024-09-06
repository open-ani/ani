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
import androidx.navigation.NavHostController
import kotlinx.coroutines.CompletableDeferred
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.datasources.api.source.FactoryId
import org.koin.core.component.KoinComponent
import org.koin.mp.KoinPlatform

/**
 * Supports navigation to any page in the app.
 *
 * 应当总是使用 [AniNavigator], 而不要访问 [navigator].
 *
 * @see LocalNavigator
 */
interface AniNavigator {
    fun setNavController(
        controller: NavHostController,
    )

    suspend fun awaitNavController(): NavHostController

    val navigator: NavHostController

    fun popBackStack() {
        navigator.popBackStack()
    }

//    fun popBackStack(
//        route: String,
//        inclusive: Boolean,
//    ) {
//        navigator.popBackStackIfExist(route, inclusive = true)
//    }

    fun popUntilNotWelcome() {
        navigator.popBackStack("/welcome", inclusive = true)
    }

    fun popUntilNotAuth() {
        navigator.popBackStack("/bangumi-token-oauth", inclusive = true)
        navigator.popBackStack("/bangumi-oauth", inclusive = true)
    }

    fun navigateSubjectDetails(subjectId: Int) {
        navigator.navigate("/subjects/$subjectId")
    }

    fun navigateSubjectCaches(subjectId: Int) {
        navigator.navigate("/subjects/$subjectId/caches")
    }

    fun navigateEpisodeDetails(subjectId: Int, episodeId: Int, fullscreen: Boolean = false) {
        navigator.popBackStack("/subjects/$subjectId/episodes/$episodeId", inclusive = true)
        navigator.navigate(
            "/subjects/$subjectId/episodes/$episodeId?fullscreen=$fullscreen",
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
        navigator.navigate("/bangumi-oauth") {
            launchSingleTop = true
        }
    }

    fun navigateBangumiTokenAuth() {
        navigator.navigate(
            "/bangumi-token-auth",
        ) {
            launchSingleTop = true
            popUpTo("/bangumi-oauth") {
                inclusive = true
            }
        }
    }

    fun navigateSettings(tab: SettingsTab = SettingsTab.Default) {
        navigator.navigate("/settings?tab=${tab.ordinal}&back=true")
    }

    fun navigateEditMediaSource(
        factoryId: FactoryId,
        existingMediaSourceInstanceId: String?,
    ) {
        navigator.navigate(
            buildString {
                append("/settings/media-source/edit?factoryId=")
                append(factoryId.value)
                append("&existingMediaSourceInstanceId=")
                existingMediaSourceInstanceId?.let {
                    append(it)
                }
            },
        )
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
    private val _navigator: CompletableDeferred<NavHostController> = CompletableDeferred()

    override val navigator: NavHostController
        get() = _navigator.getCompleted()

    override fun setNavController(controller: NavHostController) {
        this._navigator.complete(controller)
    }

    override suspend fun awaitNavController(): NavHostController {
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
