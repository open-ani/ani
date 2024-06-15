package me.him188.ani.app.ui.foundation

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale


val DEFAULT_BACKGROUND_BRUSH = Brush.verticalGradient(
    0f to Color(0xB2FAFAFA),
    1.00f to Color(0xFFFAFAFA),
)


/**
 * 添加渐变图片背景或默认纯色背景
 * @param data 图片资源, [ImageRequest.data]
 * @param fallbackColor 无图片时的纯色
 */
fun Modifier.backgroundWithGradient(
    data: Any?,
    fallbackColor: Color,
    brush: Brush = DEFAULT_BACKGROUND_BRUSH,
) =
    if (data == null) {
        composed {
            background(fallbackColor)
        }
    } else {
        paintBackground(data).background(brush = brush)
    }


/**
 * 添加图片背景或默认纯色背景
 * @param data 图片资源, [ImageRequest.data]
 * @param fallbackColor 无图片时的纯色
 */
fun Modifier.backgroundOrFallback(painter: Painter?, fallbackColor: Color) =
    if (painter == null) {
        composed {
            background(fallbackColor)
        }
    } else {
        paintBackground(painter)
    }

fun Modifier.paintBackground(painter: Painter): Modifier = composed {
    paint(
        painter,
        contentScale = ContentScale.Crop,
    )
}

fun Modifier.paintBackground(data: Any?): Modifier = composed {
    paint(
        coil3.compose.rememberAsyncImagePainter(
            data,
            LocalImageLoader.current,
        ),
        contentScale = ContentScale.Crop,
    )
}
