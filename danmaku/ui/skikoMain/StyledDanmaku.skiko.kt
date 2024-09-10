package me.him188.ani.danmaku.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextPainter
import org.jetbrains.skia.Surface

internal actual fun createDanmakuImageBitmap(
    solidTextLayout: TextLayoutResult,
    borderTextLayout: TextLayoutResult,
): ImageBitmap {
    val destSurface = Surface.makeRasterN32Premul(
        width = solidTextLayout.size.width, 
        height = solidTextLayout.size.height
    )
    val destCanvas = destSurface.canvas.asComposeCanvas()
    
    TextPainter.paint(destCanvas, borderTextLayout)
    TextPainter.paint(destCanvas, solidTextLayout)
    
    return destSurface.makeImageSnapshot().toComposeImageBitmap().apply { 
        prepareToDraw()
    }
}