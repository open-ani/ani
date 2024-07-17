@file:Suppress(
    "RedundantVisibilityModifier", "ObjectPropertyName", "UnusedReceiverParameter",
    "FloatingPointLiteralPrecision",
)

package me.him188.ani.app.ui.settings.rendering

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Rounded.Emby: ImageVector
    get() {
        if (_embyicon != null) {
            return _embyicon!!
        }
        _embyicon = Builder(
            name = "rounded.Emby",
            defaultWidth = 512.0.dp,
            defaultHeight = 512.0.dp,
            viewportWidth = 48.0f,
            viewportHeight = 48.0f,
        ).apply {
            path(
                fill = linearGradient(
                    0.0f to Color(0xFF21AD64), 1.0f to Color(0xFF088242),
                    start =
                    Offset(13.96f, 12.88f),
                    end = Offset(35.54f, 34.46f),
                ),
                stroke = null,
                strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = NonZero,
            ) {
                moveTo(36.6f, 31.4f)
                lineToRelative(1.0f, 1.0f)
                lineToRelative(-11.1f, 11.1f)
                lineToRelative(-9.0f, -9.0f)
                lineToRelative(-2.0f, 2.0f)
                lineToRelative(-11.1f, -11.1f)
                lineToRelative(9.5f, -9.5f)
                lineToRelative(-1.5f, -1.5f)
                lineToRelative(11.1f, -11.1f)
                lineToRelative(9.5f, 9.5f)
                lineToRelative(1.5f, -1.5f)
                lineToRelative(11.1f, 11.1f)
                close()
            }
            path(
                fill = linearGradient(
                    0.0f to Color(0xFFFCFCFC), 1.0f to Color(0xFFC3C9CD),
                    start =
                    Offset(26.817f, 16.069f),
                    end = Offset(26.817f, 31.845f),
                ),
                stroke = null,
                strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = NonZero,
            ) {
                moveTo(20.2f, 16.2f)
                lineToRelative(13.2f, 7.5f)
                lineToRelative(-13.2f, 7.7f)
                close()
            }
        }
            .build()
        return _embyicon!!
    }


@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = Icons.Rounded.Emby,
            contentDescription = null,
            modifier = Modifier
                .width((512.0).dp)
                .height((512.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _embyicon: ImageVector? = null
