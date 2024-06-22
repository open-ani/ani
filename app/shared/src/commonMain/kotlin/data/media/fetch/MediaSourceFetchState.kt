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


    sealed class Completed : MediaSourceFetchState()
    data object Succeed : Completed()

    /**
     * The data source upstream has failed. E.g. a network request failed.
     */
    data class Failed(
        val cause: Throwable,
    ) : Completed()

    /**
     * Failed because the flow collector has thrown an exception (and stopped collection)
     */
    data class Abandoned(
        val cause: Throwable,
    ) : Completed()
}

val MediaSourceFetchState.isWorking get() = this is MediaSourceFetchState.Working
val MediaSourceFetchState.isDisabled get() = this is MediaSourceFetchState.Disabled
val MediaSourceFetchState.isFailedOrAbandoned get() = this is MediaSourceFetchState.Failed || this is MediaSourceFetchState.Abandoned
