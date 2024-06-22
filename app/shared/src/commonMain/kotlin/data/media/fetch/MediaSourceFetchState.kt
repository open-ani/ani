package me.him188.ani.app.data.media.fetch

import androidx.compose.runtime.Stable

/**
 * @see MediaSourceFetchResult.state
 */
@Stable
sealed class MediaSourceFetchState {
    data object Idle : MediaSourceFetchState()

    /**
     * 被禁用, 因此不会主动发起请求. 仍然可以通过 [MediaSourceFetchResult.restart] 发起请求.
     */
    data object Disabled : MediaSourceFetchState()

    data object Working : MediaSourceFetchState()

    /**
     * 即将要 [Succeed], 还有一些清理工作正在进行
     */
    data class PendingSuccess(
        override val id: Int, // restartCount
    ) : Completed()

    sealed class Completed : MediaSourceFetchState() {
        internal abstract val id: Int // restartCount
    }

    data class Succeed(
        override val id: Int, // restartCount
    ) : Completed()

    /**
     * The data source upstream has failed. E.g. a network request failed.
     */
    data class Failed(
        val cause: Throwable, override val id: Int,
    ) : Completed()

    /**
     * Failed because the flow collector has thrown an exception (and stopped collection)
     */
    data class Abandoned(
        val cause: Throwable, override val id: Int,
    ) : Completed()
}

val MediaSourceFetchState.isWorking get() = this is MediaSourceFetchState.Working
val MediaSourceFetchState.isDisabled get() = this is MediaSourceFetchState.Disabled
val MediaSourceFetchState.isFailedOrAbandoned get() = this is MediaSourceFetchState.Failed || this is MediaSourceFetchState.Abandoned
