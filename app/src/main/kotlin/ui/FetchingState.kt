package me.him188.animationgarden.desktop.ui

sealed class FetchingState {
    object Idle : FetchingState()
    object Fetching : FetchingState()
    sealed class Completed : FetchingState()
    object Succeed : Completed()
    class Failed(
        val exception: Throwable
    ) : Completed()
}