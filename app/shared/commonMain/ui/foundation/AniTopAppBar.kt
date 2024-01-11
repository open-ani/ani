package me.him188.ani.app.ui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AniTopAppBar(
    modifier: Modifier = Modifier,
    paddingStart: Dp = 16.dp,
    actions: @Composable (RowScope.() -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
) {
    Box(
        Modifier.fillMaxWidth().background(containerColor),
        contentAlignment = Alignment.BottomStart
    ) {
        Row(
            modifier
                .padding(vertical = 6.dp)
                .padding(end = 16.dp)
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions?.let {
                Row(Modifier.padding(start = paddingStart)) {
                    it()
                }
            }

            title?.let {
                Row(Modifier.padding(start = paddingStart)) {
                    it()
                }
            }
//                Spacer(Modifier.weight(1f, fill = false))
//                Icon(
//                    Icons.Outlined.MoreVert,
//                    null,
//                    Modifier.size(24.dp)
//                )
        }
    }
}

@Composable
fun TopAppBarGoBackButton(goBack: () -> Unit) {
    TopAppBarActionButton(goBack) {
        Icon(
            Icons.Outlined.ArrowBack,
            null,
            Modifier.size(24.dp)
        )
    }
}

@Composable
fun TopAppBarActionButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick,
        Modifier.offset(x = (-8).dp, y = (-8).dp).width(36.dp + 16.dp).height(36.dp + 16.dp)
    ) { // 让可点击区域大一点, 更方便
        Box(Modifier.size(24.dp)) {
            content()
        }
    }
}
