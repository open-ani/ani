package me.him188.ani.danmaku.ui

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextPainter
import android.graphics.Canvas as AndroidCanvas

// create a gpu-accelerated bitmap
internal actual fun createDanmakuImageBitmap(
    solidTextLayout: TextLayoutResult,
    borderTextLayout: TextLayoutResult,
): ImageBitmap {
    val destBitmap = Bitmap.createBitmap(
        borderTextLayout.size.width, 
        borderTextLayout.size.height, 
        Bitmap.Config.ARGB_8888
    )
    val destCanvas = Canvas(AndroidCanvas(destBitmap))
    
    TextPainter.paint(destCanvas, borderTextLayout)
    TextPainter.paint(destCanvas, solidTextLayout)
    
    return destBitmap.asImageBitmap().apply { 
        prepareToDraw()
    }
}