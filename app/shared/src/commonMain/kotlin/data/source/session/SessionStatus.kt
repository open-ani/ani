package me.him188.ani.app.data.source.session

import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.data.source.session.SessionStatus.NoToken
import me.him188.ani.app.data.source.session.SessionStatus.Refreshing
import me.him188.ani.app.data.source.session.SessionStatus.VerificationFailed
import me.him188.ani.app.data.source.session.SessionStatus.Verified
import me.him188.ani.app.data.source.session.SessionStatus.Verifying

/**
 * 表示登录会话的状态.
 *
 * 状态主要分为这几类:
 * - [NoToken]: 没有保存的 access token, 也就是用户还没尝试过登录, 或者点击了退出登录.
 * - [Verifying]: 正在进行初次登录, 或者正在使用 refresh token 更换新 access token, 或者正在验证 access token 的有效性.
 * - [Refreshing]: 正在使用 refresh token 更换新 access token
 * - [Verified]: 登录完全成功. 这意味着已经连接到服务器测试过 token 是有效的.
 * - [VerificationFailed]: 有 token, 但是验证失败, 细分为一些详细原因.
 */
sealed interface SessionStatus {
    /**
     * 一个拥有 access token 的状态, 但这个 token 是未经过验证的.
     */
    sealed interface HasAccessToken : SessionStatus {
        val accessTokenMaybeUnverified: String
    }

    /**
     * 表示正在加载中的中间状态. [Refreshing] 或者 [Verifying].
     */
    sealed interface Loading : SessionStatus

    /**
     * 正在使用 refresh token 换取新的 access token
     */
    data object Refreshing : SessionStatus, Loading

    /**
     * 正在进行初次登录, 或者正在验证 access token 的有效性.
     */
    data class Verifying(
        override val accessTokenMaybeUnverified: String,
    ) : HasAccessToken, Loading

    /**
     * 登录完全成功. 这意味着已经连接到服务器测试过 token 是有效的.
     */
    data class Verified(
        override val accessTokenMaybeUnverified: String,
        val userInfo: UserInfo,
    ) : SessionStatus, HasAccessToken {
        val accessToken get() = accessTokenMaybeUnverified
    }

    /**
     * 用户希望以游客身份登录
     */
    data object Guest : SessionStatus

    /**
     * 有 token, 但是验证失败
     */
    sealed interface VerificationFailed : SessionStatus

    /**
     * 有 token, 但是验证失败, 因为 token 过期了.
     *
     * 仅当同时没有 refresh token, 或者使用 refresh token 也失败了才会有此状态.
     */
    data object Expired : VerificationFailed

    /**
     * 有 token, 但是验证失败, 因为网络问题
     */
    data object NetworkError : VerificationFailed

    /**
     * 有 token, 但是验证失败, 因为服务器炸了
     */
    data object ServiceUnavailable : VerificationFailed

    /**
     * 没有 (保存的) token
     */
    data object NoToken : VerificationFailed
}

/**
 * 获取未经验证的 access token. 未经验证, 也就是说这个 token 可能是:
 * - 是有效的
 * - 已经过期
 * - 没过期, 但是在服务器上已经无效了 (比如因为用户更改了密码)
 */
val SessionStatus.unverifiedAccessTokenOrNull: String? get() = (this as? SessionStatus.HasAccessToken)?.accessTokenMaybeUnverified

/**
 * 获取当前经过验证的用户信息. 如果用户未登录, 则返回 `null`.
 */
val SessionStatus.userInfoOrNull: UserInfo? get() = (this as? Verified)?.userInfo

/**
 * 获取当前经过验证的登录用户的用户名. 如果用户未登录, 则返回 `null`.
 */
val SessionStatus.usernameOrNull: String? get() = userInfoOrNull?.username
