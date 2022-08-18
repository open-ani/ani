package me.him188.animationgarden.desktop.app

import me.him188.animationgarden.api.model.SearchQuery

sealed class FetchingState {
    object Idle : FetchingState()
    class Fetching(
        val query: SearchQuery
    ) : FetchingState()

    sealed class Completed : FetchingState()
    object Succeed : Completed()
    class Failed(
        val exception: Throwable
    ) : Completed()
}