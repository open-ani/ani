package me.him188.ani.app.data.source.session

import me.him188.ani.app.data.models.UserInfo

sealed interface SessionState {
    sealed interface HasAccessToken : SessionState {
        val accessToken: String
    }

    /**
     * 登录完全成功. 这意味着已经连接到服务器测试过 token 是有效的.
     */
    data class Verified(
        override val accessToken: String,
        val userInfo: UserInfo,
    ) : SessionState, HasAccessToken

    /**
     * 有 token, 但是验证失败
     */
    sealed interface VerificationFailed : SessionState

    /**
     * 有 token, 但是验证失败, 因为 token 过期了
     */
    data object Expired : VerificationFailed

    /**
     * 有 token, 但是验证失败, 因为网络问题
     */
    data object NetworkError : VerificationFailed

    /**
     * 有 token, 但是验证失败, 因为 bug
     */
    data class Exception(
        val cause: Throwable,
    ) : VerificationFailed

    /**
     * 没有 token
     */
    data object NoToken : SessionState
}

fun SessionState.isValid(): Boolean = this is SessionState.Verified

val SessionState.unverifiedAccessToken: String? get() = (this as? SessionState.HasAccessToken)?.accessToken
val SessionState.userInfo: UserInfo? get() = (this as? SessionState.Verified)?.userInfo
val SessionState.username: String? get() = userInfo?.username
