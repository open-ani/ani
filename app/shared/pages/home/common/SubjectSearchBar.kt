package me.him188.ani.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import me.him188.ani.app.OneshotTip
import me.him188.ani.app.interaction.clearFocusOnKeyboardDismiss
import me.him188.ani.app.ui.foundation.AnimatedFilterChip

@Composable
fun SubjectSearchBar(
    initialActive: Boolean = false,
    initialSearchText: String = "",
    editingTagMode: Boolean,
    searchTag: List<SearchTag>,
    showDeleteTagTip: Boolean,
    searchHistory: List<SearchHistory>,
    modifier: Modifier = Modifier,
    onActiveChange: (Boolean) -> Unit,
    onToggleTag: (Int, Boolean) -> Unit,
    onAddTag: (String) -> Unit,
    onDeleteTag: (Int) -> Unit,
    onDeleteHistory: (Int) -> Unit,
    onDisableDeleteTagTip: () -> Unit,
    onStartEditingTagMode: () -> Unit,
    onSearch: (String, Boolean) -> Unit,
) {
    var isActive by remember { mutableStateOf(initialActive) }
    var searchText by remember { mutableStateOf(initialSearchText) }
    var newCustomFilterDialogOpened by remember { mutableStateOf(false) }
    
    val keyboard by rememberUpdatedState(LocalSoftwareKeyboardController.current)

    val horizontalPadding by animateDpAsState(
        targetValue = if (isActive) 0.dp else 16.dp,
        animationSpec = tween(easing = FastOutSlowInEasing)
    )
    val shapeSize by animateDpAsState(
        targetValue = if (isActive) 0.dp else 28.0.dp,
        animationSpec = tween(easing = FastOutSlowInEasing)
    )

    fun toggleActive(value: Boolean? = null) {
        isActive = value ?: !isActive
        onActiveChange(isActive)
    }

    NewCustomSearchFilterDialog(
        openDialog = newCustomFilterDialogOpened,
        onConfirm = { newTag ->
            newCustomFilterDialogOpened = false
            onAddTag(newTag)
        },
        onDismiss = { newCustomFilterDialogOpened = false }
    )
    
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
            .then(modifier),
        onActiveChange = { toggleActive(it) },
        onQueryChange = { searchText = it },
        onSearch = {
            if (!editingTagMode) {
                toggleActive(false)
                onSearch(it, false)
            }
            keyboard?.hide()
        },
        
    ) {
        Box(
            modifier = Modifier.fillMaxSize().imePadding()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp)
            ) {
                item("tag_tag") {
                    Text(
                        text = "标签",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item("tag_row") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        searchTag.forEach { tag ->
                            AnimatedFilterChip(
                                selected = tag.checked && !editingTagMode,
                                label = { Text(text = tag.content) },
                                trailingIcon = {
                                    AnimatedVisibility(
                                        visible = editingTagMode,
                                        enter = expandHorizontally(),
                                        exit = shrinkHorizontally()
                                    ) {
                                        IconButton(
                                            onClick = { onDeleteTag(tag.id) },
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Close,
                                                contentDescription = "",
                                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                                            )
                                        }
                                    }
                                },
                                onClick = { onToggleTag(tag.id, !tag.checked) },
                            )
                        }
                        AnimatedVisibility(
                            visible = !editingTagMode,
                            enter = expandHorizontally(),
                            exit = shrinkHorizontally()
                        ) {
                            AddNewTagChip(
                                // TODO: Chip composable 好像有 vertical padding 不知道从哪里来的。
                                // 我们自定义的支持长按的 chip 也需要加上。
                                modifier = Modifier.padding(vertical = 8.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                onLongClick = {
                                    keyboard?.hide()
                                    onStartEditingTagMode()
                                },
                                onClick = { newCustomFilterDialogOpened = true }
                            )
                        }
                    }
                }
                if (showDeleteTagTip) {
                    item("tag_tip") {
                        OneshotTip(
                            text = "您可以点击加号标签添加新搜索标签，长按加号标签进入标签删除模式。",
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            onClose = onDisableDeleteTagTip
                        )
                    }
                }

                item("tag_history") {
                    Text(
                        text = "搜索历史",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item("history") {
                    val verticalPadding by animateDpAsState(
                        targetValue = if (searchHistory.isEmpty()) 24.dp else 0.dp,
                        animationSpec = tween(easing = FastOutSlowInEasing)
                    )
                    Column(
                        modifier = Modifier.padding(vertical = verticalPadding)
                    ) {
                        AnimatedVisibility(
                            visible = !editingTagMode,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            if (searchHistory.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "无搜索历史",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    searchHistory.forEach { history ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    toggleActive()
                                                    searchText = history.content
                                                    onSearch(history.content, true)
                                                    keyboard?.hide()
                                                }
                                                .padding(vertical = 12.dp)
                                                .padding(start = 16.dp, end = 8.dp)
                                                .animateItemPlacement(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = history.content,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            IconButton(
                                                modifier = Modifier.size(32.dp),
                                                onClick = { onDeleteHistory(history.id) }
                                            ) {
                                                Icon(
                                                    modifier = Modifier.size(22.dp),
                                                    imageVector = Icons.Outlined.Close,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * simplify from FilterChip
 */
@Composable
private fun AddNewTagChip(
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {

    Surface(
        modifier = modifier.clip(FilterChipDefaults.shape).combinedClickable(
            role = Role.Checkbox,
            onClick = onClick,
            onLongClick = onLongClick
        ),
        color = containerColor,
        shape = FilterChipDefaults.shape,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = FilterChipDefaults.Height)
                .padding(16.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                    content = { Icon(Icons.Outlined.Add, contentDescription = null) }
                )
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NewCustomSearchFilterDialog(
    openDialog: Boolean,
    modifier: Modifier = Modifier,
    onConfirm: (text: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    if (openDialog) {
        var text by remember {
            mutableStateOf("")
        }
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        if (text.isBlank()) {
                            onDismiss()
                        } else {
                            onConfirm(text)
                        }
                    },
                ) {
                    Text(text = "确认")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "取消")
                }
            },
            icon = { Icon(imageVector = Icons.Outlined.NewLabel, contentDescription = null) },
            title = {
                Text(text = "新增搜索标签")
            },
            text = {
                Column(
                    modifier = modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.clearFocusOnKeyboardDismiss(),
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("自定义标签") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                if (text.isBlank()) {
                                    onDismiss()
                                } else {
                                    onConfirm(text)
                                }
                            }
                        )
                    )
                    Text(
                        text = "新增自定义搜索标签，手动添加搜索结果过滤。" +
                                "除此之外您也可以在搜索结果界面将标签自动添加至搜索过滤标签。"
                    )
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = true
            ),
        )
    }
}