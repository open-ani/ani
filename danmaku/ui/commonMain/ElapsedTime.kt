package me.him188.ani.danmaku.ui

import kotlin.jvm.JvmInline

// Elapsed time is used at [interpolateFrame].
// higher 32 bit = time, lower 32 bit = count
@JvmInline
internal value class ElapsedFrame private constructor(private val packed: Long) {
    constructor(time: Long, count: Int) : this((time shl 32) or count.toLong())

    fun addDelta(delta: Long): ElapsedFrame {
        var time = packed shr 32
        time += delta
        return ElapsedFrame((time shl 32) or (packed and 0x0000_0000_ffff_ffff) + 1)
    }

    fun avg(): Long {
        val time = packed shr 32
        val count = packed and 0x0000_0000_ffff_ffff

        if (count == 0L) return 0L
        return time / count
    }
    
    companion object {
        fun zero(): ElapsedFrame {
            return ElapsedFrame(0)
        }
    }
}