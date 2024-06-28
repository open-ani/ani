package me.him188.ani.app.ui.foundation.avatar

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.ktor.http.URLBuilder

@Stable
fun defaultAvatar(
    name: String,
    backgroundColor: Color,
    color: Color,
): String {
    return URLBuilder("https://ui-avatars.com/api/").apply {
        parameters.append("name", name)
        parameters.append(
            "background",
            backgroundColor.toRgbString(),
        )
        parameters.append(
            "color",
            color.toRgbString(),
        )
    }.buildString()
}

@Stable
private fun Color.toRgbString() = toArgb().and(0xFFFFFF).toString(16).padStart(6, '0')
