package me.him188.ani.app.ui.foundation.avatar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import me.him188.ani.app.ui.foundation.AsyncImage
import java.io.File

@Composable
fun AvatarImage(
    url: String?,
    modifier: Modifier = Modifier,
    filePath: String? = null,
    colorFilter: ColorFilter? = null,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
) {
    AsyncImage(
        model = if (filePath != null) File(filePath) else url,
        contentDescription = "Avatar",
        modifier = modifier,
        error = rememberVectorPainter(Icons.Rounded.Person),
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = colorFilter,
    )
}
