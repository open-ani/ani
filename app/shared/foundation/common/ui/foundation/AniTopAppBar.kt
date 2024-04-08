package me.him188.ani.app.ui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.navigation.LocalBackHandler

@Composable
fun AniTopAppBar(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 6.dp),
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
                .padding(padding)
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions?.let {
                Row(Modifier, verticalAlignment = Alignment.CenterVertically) {
                    it()
                }
            }

            title?.let {
                Row(Modifier, verticalAlignment = Alignment.CenterVertically) {
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
fun TopAppBarGoBackButton() {
    val handler = LocalBackHandler.current
    TopAppBarGoBackButton {
        handler.onBackPress()
    }
}

@Composable
fun TopAppBarGoBackButton(goBack: () -> Unit) {
    TopAppBarActionButton(goBack) {
        Icon(
            Icons.AutoMirrored.Outlined.ArrowBack,
            null,
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
//        Modifier.offset(x = (-8).dp, y = (-8).dp).width(36.dp + 16.dp).height(36.dp + 16.dp)
    ) { // 让可点击区域大一点, 更方便
        Box(Modifier.size(24.dp)) {
            content()
        }
    }
}
