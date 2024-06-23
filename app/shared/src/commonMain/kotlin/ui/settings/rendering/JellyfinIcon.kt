@file:Suppress(
    "RedundantVisibilityModifier", "ObjectPropertyName", "UnusedReceiverParameter",
    "FloatingPointLiteralPrecision",
)

package me.him188.ani.app.ui.icons

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Rounded.Jellyfin: ImageVector
    get() {
        val current = _jellyfin
        if (current != null) return current

        return ImageVector.Builder(
            name = "rounded.Jellyfin",
            defaultWidth = 512.0.dp,
            defaultHeight = 512.0.dp,
            viewportWidth = 512.0f,
            viewportHeight = 512.0f,
        ).apply {
            // <rect width="512" height="512" fill="#fff" />
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
            ) {
                // M 0 0
                moveTo(x = 0.0f, y = 0.0f)
                // h 512
                horizontalLineToRelative(dx = 512.0f)
                // v 512
                verticalLineToRelative(dy = 512.0f)
                // h -512z
                horizontalLineToRelative(dx = -512.0f)
                close()
            }
            // M190.56 329.07 c8.63 17.3 122.4 17.12 130.93 0 8.52 -17.1 -47.9 -119.78 -65.46 -119.8 -17.57 0 -74.1 102.5 -65.47 119.8
            path(
                fill = Brush.linearGradient(
                    0.0f to Color(0xFFAA5CC3),
                    1.0f to Color(0xFF00A4DC),
                    start = Offset(x = 126.15f, y = 219.32f),
                    end = Offset(x = 457.68f, y = 410.73f),
                ),
            ) {
                // M 190.56 329.07
                moveTo(x = 190.56f, y = 329.07f)
                // c 8.63 17.3 122.4 17.12 130.93 0
                curveToRelative(
                    dx1 = 8.63f,
                    dy1 = 17.3f,
                    dx2 = 122.4f,
                    dy2 = 17.12f,
                    dx3 = 130.93f,
                    dy3 = 0.0f,
                )
                // c 8.52 -17.1 -47.9 -119.78 -65.46 -119.8
                curveToRelative(
                    dx1 = 8.52f,
                    dy1 = -17.1f,
                    dx2 = -47.9f,
                    dy2 = -119.78f,
                    dx3 = -65.46f,
                    dy3 = -119.8f,
                )
                // c -17.57 0 -74.1 102.5 -65.47 119.8
                curveToRelative(
                    dx1 = -17.57f,
                    dy1 = 0.0f,
                    dx2 = -74.1f,
                    dy2 = 102.5f,
                    dx3 = -65.47f,
                    dy3 = 119.8f,
                )
            }
            // M190.56 329.07 c8.63 17.3 122.4 17.12 130.93 0 8.52 -17.1 -47.9 -119.78 -65.46 -119.8 -17.57 0 -74.1 102.5 -65.47 119.8
            path {
                // M 190.56 329.07
                moveTo(x = 190.56f, y = 329.07f)
                // c 8.63 17.3 122.4 17.12 130.93 0
                curveToRelative(
                    dx1 = 8.63f,
                    dy1 = 17.3f,
                    dx2 = 122.4f,
                    dy2 = 17.12f,
                    dx3 = 130.93f,
                    dy3 = 0.0f,
                )
                // c 8.52 -17.1 -47.9 -119.78 -65.46 -119.8
                curveToRelative(
                    dx1 = 8.52f,
                    dy1 = -17.1f,
                    dx2 = -47.9f,
                    dy2 = -119.78f,
                    dx3 = -65.46f,
                    dy3 = -119.8f,
                )
                // c -17.57 0 -74.1 102.5 -65.47 119.8
                curveToRelative(
                    dx1 = -17.57f,
                    dy1 = 0.0f,
                    dx2 = -74.1f,
                    dy2 = 102.5f,
                    dx3 = -65.47f,
                    dy3 = 119.8f,
                )
            }
            // M58.75 417.03 c25.97 52.15 368.86 51.55 394.55 0 S308.93 56.08 256.03 56.08 c-52.92 0 -223.25 308.8 -197.28 360.95 m68.04 -45.25 c-17.02 -34.17 94.6 -236.5 129.26 -236.5 34.67 0 146.1 202.7 129.26 236.5 -16.83 33.8 -241.5 34.17 -258.52 0
            path(
                fill = Brush.linearGradient(
                    0.0f to Color(0xFFAA5CC3),
                    1.0f to Color(0xFF00A4DC),
                    start = Offset(x = 126.15f, y = 219.32f),
                    end = Offset(x = 457.68f, y = 410.73f),
                ),
            ) {
                // M 58.75 417.03
                moveTo(x = 58.75f, y = 417.03f)
                // c 25.97 52.15 368.86 51.55 394.55 0
                curveToRelative(
                    dx1 = 25.97f,
                    dy1 = 52.15f,
                    dx2 = 368.86f,
                    dy2 = 51.55f,
                    dx3 = 394.55f,
                    dy3 = 0.0f,
                )
                // S 308.93 56.08 256.03 56.08
                reflectiveCurveTo(
                    x1 = 308.93f,
                    y1 = 56.08f,
                    x2 = 256.03f,
                    y2 = 56.08f,
                )
                // c -52.92 0 -223.25 308.8 -197.28 360.95
                curveToRelative(
                    dx1 = -52.92f,
                    dy1 = 0.0f,
                    dx2 = -223.25f,
                    dy2 = 308.8f,
                    dx3 = -197.28f,
                    dy3 = 360.95f,
                )
                // m 68.04 -45.25
                moveToRelative(dx = 68.04f, dy = -45.25f)
                // c -17.02 -34.17 94.6 -236.5 129.26 -236.5
                curveToRelative(
                    dx1 = -17.02f,
                    dy1 = -34.17f,
                    dx2 = 94.6f,
                    dy2 = -236.5f,
                    dx3 = 129.26f,
                    dy3 = -236.5f,
                )
                // c 34.67 0 146.1 202.7 129.26 236.5
                curveToRelative(
                    dx1 = 34.67f,
                    dy1 = 0.0f,
                    dx2 = 146.1f,
                    dy2 = 202.7f,
                    dx3 = 129.26f,
                    dy3 = 236.5f,
                )
                // c -16.83 33.8 -241.5 34.17 -258.52 0
                curveToRelative(
                    dx1 = -16.83f,
                    dy1 = 33.8f,
                    dx2 = -241.5f,
                    dy2 = 34.17f,
                    dx3 = -258.52f,
                    dy3 = 0.0f,
                )
            }
            // M58.75 417.03 c25.97 52.15 368.86 51.55 394.55 0 S308.93 56.08 256.03 56.08 c-52.92 0 -223.25 308.8 -197.28 360.95 m68.04 -45.25 c-17.02 -34.17 94.6 -236.5 129.26 -236.5 34.67 0 146.1 202.7 129.26 236.5 -16.83 33.8 -241.5 34.17 -258.52 0
            path {
                // M 58.75 417.03
                moveTo(x = 58.75f, y = 417.03f)
                // c 25.97 52.15 368.86 51.55 394.55 0
                curveToRelative(
                    dx1 = 25.97f,
                    dy1 = 52.15f,
                    dx2 = 368.86f,
                    dy2 = 51.55f,
                    dx3 = 394.55f,
                    dy3 = 0.0f,
                )
                // S 308.93 56.08 256.03 56.08
                reflectiveCurveTo(
                    x1 = 308.93f,
                    y1 = 56.08f,
                    x2 = 256.03f,
                    y2 = 56.08f,
                )
                // c -52.92 0 -223.25 308.8 -197.28 360.95
                curveToRelative(
                    dx1 = -52.92f,
                    dy1 = 0.0f,
                    dx2 = -223.25f,
                    dy2 = 308.8f,
                    dx3 = -197.28f,
                    dy3 = 360.95f,
                )
                // m 68.04 -45.25
                moveToRelative(dx = 68.04f, dy = -45.25f)
                // c -17.02 -34.17 94.6 -236.5 129.26 -236.5
                curveToRelative(
                    dx1 = -17.02f,
                    dy1 = -34.17f,
                    dx2 = 94.6f,
                    dy2 = -236.5f,
                    dx3 = 129.26f,
                    dy3 = -236.5f,
                )
                // c 34.67 0 146.1 202.7 129.26 236.5
                curveToRelative(
                    dx1 = 34.67f,
                    dy1 = 0.0f,
                    dx2 = 146.1f,
                    dy2 = 202.7f,
                    dx3 = 129.26f,
                    dy3 = 236.5f,
                )
                // c -16.83 33.8 -241.5 34.17 -258.52 0
                curveToRelative(
                    dx1 = -16.83f,
                    dy1 = 33.8f,
                    dx2 = -241.5f,
                    dy2 = 34.17f,
                    dx3 = -258.52f,
                    dy3 = 0.0f,
                )
            }
        }.build().also { _jellyfin = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = Icons.Rounded.Jellyfin,
            contentDescription = null,
            modifier = Modifier
                .width((512.0).dp)
                .height((512.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _jellyfin: ImageVector? = null
