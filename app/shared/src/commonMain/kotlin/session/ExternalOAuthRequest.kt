package me.him188.ani.app.session

import androidx.annotation.WorkerThread
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import me.him188.ani.app.navigation.AniNavigator
import org.koin.core.component.KoinComponent

/**
 * 表示一个外部 OAuth 授权请求.
 * 发起外部浏览器等进行 OAuth. 适用于第一次登录, 或者 refresh token 已经过期的情况.
 *
 * 此请求在需要用户登录时创建, 封装 OAuth 的逻辑.
 */
interface ExternalOAuthRequest {
    val state: StateFlow<State>

    /**
     * OAuth 成功回调 code 时调用.
     */
    fun onCallback(code: Result<OAuthResult>)

    fun cancel()

    /**
     * Does not throw
     */
    @WorkerThread
    suspend fun invoke()

    sealed class State {
        data object Launching : State()
        data object AwaitingCallback : State()

        /**
         * Processing callback. E.g. obtaining access token using the code
         */
        data object Processing : State()

        sealed class Result : State()

        data class Cancelled(
            val cause: CancellationException
        ) : Result()

        /**
         * Failed to obtain access token using the code
         */
        data class Failed(
            val throwable: Throwable
        ) : Result()

        data object Success : Result()
    }
}

data class OAuthResult(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
)

internal class BangumiOAuthRequest(
    private val aniNavigator: AniNavigator,
    private val navigateToWelcome: Boolean,
    private val setSession: suspend (NewSession) -> Unit,
) : ExternalOAuthRequest, KoinComponent {
    override val state: MutableStateFlow<ExternalOAuthRequest.State> =
        MutableStateFlow(ExternalOAuthRequest.State.Launching)

    private val result = CompletableDeferred<OAuthResult>()

    /**
     * OAuth 成功回调 code 时调用.
     */
    override fun onCallback(code: Result<OAuthResult>) {
        this.result.completeWith(code)
    }

    override fun cancel() {
        result.cancel()
    }

    override suspend fun invoke() {
        state.value = ExternalOAuthRequest.State.Launching
        withContext(Dispatchers.Main) {
            if (navigateToWelcome) {
                aniNavigator.navigateWelcome()
            } else {
                aniNavigator.navigateBangumiOAuthOrTokenAuth()
            }
        }
        state.value = ExternalOAuthRequest.State.AwaitingCallback
        try {
            val result = result.await()
            state.value = ExternalOAuthRequest.State.Processing

            setSession(
                NewSession(
                    result.accessToken,
                    System.currentTimeMillis() + result.expiresInSeconds * 1000,
                    result.refreshToken,
                ),
            )
        } catch (e: CancellationException) {
            state.value = ExternalOAuthRequest.State.Cancelled(e)
            return
        } catch (e: Throwable) {
            state.value = ExternalOAuthRequest.State.Failed(e)
            return
        }
        state.value = ExternalOAuthRequest.State.Success
    }
}
