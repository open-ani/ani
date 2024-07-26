package me.him188.ani.app.data.source.session

sealed interface SessionEvent {
    /**
     * token 有变更
     */
    sealed interface UserActionEvent : SessionEvent
    data object Login : UserActionEvent
    data object Logout : UserActionEvent

    data object TokenRefreshed : SessionEvent
}