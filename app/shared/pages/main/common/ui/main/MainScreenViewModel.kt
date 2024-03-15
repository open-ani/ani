package me.him188.ani.app.ui.main

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.him188.ani.app.ui.foundation.AbstractViewModel

class MainScreenViewModel : AbstractViewModel() {
    private val _showSearchBar: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val showSearchBar: StateFlow<Boolean> get() = _showSearchBar
}