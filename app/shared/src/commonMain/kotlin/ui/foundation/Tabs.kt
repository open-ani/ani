package me.him188.animationgarden.app.ui.foundation

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier

class TabState(
    initialSelected: String
) {
    private var _selected: String by mutableStateOf(initialSelected)
    val selected: String get() = _selected

    fun switchTo(name: String) {
        _selected = name
    }
}

@Composable
fun rememberTabState(initialSelected: String): TabState {
    return rememberSaveable { TabState(initialSelected) }
}

class TabScope(
    @PublishedApi
    internal val tabState: TabState,
) {
    class Tab(
        val name: String,
        val content: @Composable (isSelected: Boolean) -> Unit
    )

    @Stable
    internal val tabs: MutableList<Tab> = SnapshotStateList()

    fun tab(
        name: String,
        content: @Composable (isSelected: Boolean) -> Unit
    ) {
        tabs.add(Tab(name, content))
    }
}

@Composable
fun TabHost(
    state: TabState,
    modifier: Modifier = Modifier,
    content: TabScope.() -> Unit
) {
    Box(modifier) {
        val scope = remember(state) {
            TabScope(state).apply(content)
        }
        scope.tabs.forEach { tab ->
            tab.content(tab.name == state.selected)
        }
    }
}

@Composable
private fun PreviewTabs() {
    val tabState = rememberTabState("home")
    TabHost(tabState) {
        tab("home") {
            Text("home")
        }
        tab("search") {
            Text("search")
        }
        tab("profile") {
            Text("profile")
        }
    }
}