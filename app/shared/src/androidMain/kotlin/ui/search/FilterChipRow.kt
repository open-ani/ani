/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.animationgarden.app.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SelectableChipElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow

@Composable
actual fun <T> FilterChipRowImpl(
    list: List<T>,
    key: (item: T) -> Any,
    isSelected: @Composable (T) -> Boolean,
    onClick: ((T) -> Unit)?,
    enabled: @Composable (T) -> Boolean,
    isExpanded: Boolean,
    elevation: SelectableChipElevation?,
    refreshState: RefreshState?,
    onClickRefreshResult: (() -> Unit)?,
    content: @Composable (T) -> Unit,
) {
    Box {
        AnimatedVisibility(
            !isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            FilterChipRowByLazyRow(
                list = list,
                key = key,
                isSelected = isSelected,
                onClick = onClick,
                enabled = enabled,
                elevation = elevation,
                refreshState = refreshState,
                onClickRefreshResult = onClickRefreshResult,
                content = content
            )
        }
        AnimatedVisibility(
            isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    visibilityThreshold = IntSize.VisibilityThreshold
                )
            )
        ) {
            FilterChipByFlowRow(
                list,
                isSelected,
                onClick,
                content,
                enabled,
                elevation,
                refreshState
            )
        }
    }
}

@Composable
private fun <T> FilterChipByFlowRow(
    list: List<T>,
    isSelected: @Composable (T) -> Boolean,
    onClick: ((T) -> Unit)?,
    content: @Composable (T) -> Unit,
    enabled: @Composable (T) -> Boolean,
    elevation: SelectableChipElevation?,
    refreshState: RefreshState?,
) {
    val showSuccessHint by animateFloatAsState(
        if (refreshState != RefreshState.Success) 1f else 0f,
        tween(2000),
        label = "showSuccessHint",
    )

    val currentOnClick by rememberUpdatedState(onClick)
    val cardHeight = 32.dp
    val textHeight = 24.sp
    val progressSize = 18.dp
    val tickSize = 24.dp

    FlowRow(
        mainAxisSpacing = 6.dp,
        crossAxisSpacing = 8.dp,
        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
        lastLineMainAxisAlignment = FlowMainAxisAlignment.Start,
    ) {
        for (it in list) {
            ElevatedFilterChip(
                selected = isSelected(it),
                onClick = { currentOnClick?.invoke(it) },
                label = {
                    ProvideTextStyle(LocalTextStyle.current.copy(lineHeight = textHeight)) {
                        content(it)
                    }
                },
                enabled = enabled.invoke(it),
                elevation = elevation,
                modifier = Modifier.height(cardHeight),
//                modifier = Modifier.animateItemPlacement(tween(200, 100)),
            )
        }
        if (refreshState != null && (refreshState != RefreshState.Success || showSuccessHint > 0)) {
            RefreshingChip(
                refreshState = refreshState,
                textHeight = textHeight,
                cardHeight = cardHeight,
                progressSize = progressSize,
                tickSize = tickSize,
                elevation = elevation
            )
        }
    }
}

@Preview(widthDp = 400, heightDp = 100)
@Preview(widthDp = 300, heightDp = 100)
@Preview(widthDp = 100, heightDp = 200)
@Composable
internal fun PreviewFilterChipRowByFlowRow() {
    data class Element(
        val content: String,
        val enabled: Boolean,
        val selected: Boolean = false,
    )

    val elements = listOf(
        Element("Foo", true),
        Element("Foo", true),
        Element("Foo", true),
        Element("Bar", false),
        Element("Zoo", true),
    )

    FilterChipByFlowRow(
        list = elements,
        isSelected = { it.selected },
        onClick = {},
        content = { Text(text = it.content) },
        enabled = { true },
        elevation = null,
        refreshState = null,
    )
}