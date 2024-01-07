package me.him188.ani.app.ui.foundation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
            .height(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(goBack, Modifier.size(24.dp)) {
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