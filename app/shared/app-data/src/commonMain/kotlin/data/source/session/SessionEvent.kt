package me.him188.ani.app.data.source.session

sealed interface SessionEvent {
    sealed interface UserChanged : SessionEvent

    data object Login : UserChanged
    data object Logout : UserChanged

    data object SwitchToGuest : SessionEvent

    data object TokenRefreshed : SessionEvent
}