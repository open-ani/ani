package me.him188.ani.app.testFramework

import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.data.repository.Settings

class MutableSettings<T>(
    initialValue: T
) : Settings<T> {
    override val flow: MutableStateFlow<T> = MutableStateFlow(initialValue)
    override suspend fun set(value: T) {
        flow.value = value
    }
}
