package me.him188.ani.app.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import me.him188.ani.app.interaction.clearFocusOnKeyboardDismiss

@Composable
fun SubjectSearchBar(
    initialActive: Boolean = false,
    initialSearchText: String = "",
    modifier: Modifier = Modifier,
    onActiveChange: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
) {
    var isActive by remember { mutableStateOf(initialActive) }
    var searchText by remember { mutableStateOf(initialSearchText) }
    val keyboard by rememberUpdatedState(LocalSoftwareKeyboardController.current)

    val horizontalPadding by animateDpAsState(
        targetValue = if (isActive) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    val shapeSize by animateDpAsState(
        targetValue = if (isActive) 0.dp else 28.0.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )

    fun toggleActive(value: Boolean? = null) {
        isActive = value ?: !isActive
        onActiveChange(isActive)
    }
    
    SearchBar(
        query = searchText,
        active = isActive,
        placeholder = { Text("搜索") },
        leadingIcon = {
            IconButton({ toggleActive() }) {
                Icon(
                    if (isActive) {
                        Icons.AutoMirrored.Outlined.ArrowBack
                    } else {
                        Icons.Outlined.Search
                    }, null
                )
            }
        },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                IconButton({ searchText = "" }) {
                    Icon(Icons.Outlined.Close, null)
                }
            }
        },
        colors = SearchBarDefaults.colors(dividerColor = Color.Transparent),
        tonalElevation = SearchBarDefaults.TonalElevation,
        shape = RoundedCornerShape(shapeSize),
        modifier = Modifier
            .padding(horizontal = horizontalPadding)
            .clearFocusOnKeyboardDismiss()
            .clickable { toggleActive() }
            .then(modifier),
        onActiveChange = { toggleActive(it) },
        onQueryChange = { searchText = it },
        onSearch = {
            toggleActive(false)
            onSearch(it)
            keyboard?.hide()
        },
        
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text("search suggestion")
        }
    }
}
