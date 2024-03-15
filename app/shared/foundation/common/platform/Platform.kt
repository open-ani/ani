package me.him188.ani.app.platform

sealed class Platform {
    abstract val name: String

    data object Android : Platform() {
        override val name: String get() = "Android"
    }

    data object Ios : Platform() {
        override val name: String get() = "iOS"
    }

    data class Desktop(
        override val name: String
    ) : Platform()

    companion object
}

expect fun Platform.Companion.currentPlatform(): Platform