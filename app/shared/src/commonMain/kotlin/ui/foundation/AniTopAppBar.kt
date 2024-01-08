package me.him188.ani.app.ui.foundation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AniTopAppBar(
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .padding(vertical = 6.dp)
            .padding(horizontal = 16.dp)
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(goBack, Modifier.offset(x = (-16).dp).width(36.dp + 32.dp).height(36.dp)) { // 让可点击区域大一点, 更方便
            Icon(
                Icons.Outlined.ArrowBack,
                null,
                Modifier.size(24.dp)
            )
        }
//                Spacer(Modifier.weight(1f, fill = false))
//                Icon(
//                    Icons.Outlined.MoreVert,
//                    null,
//                    Modifier.size(24.dp)
//                )
    }

}