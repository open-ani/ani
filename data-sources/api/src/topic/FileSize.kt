/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("NOTHING_TO_INLINE")

package me.him188.ani.datasources.api.topic

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class FileSize(
    val inBytes: Long,
) {
    inline val inKiloBytes: Long get() = inKiloBytesDouble.toLong()
    inline val inMegaBytes: Long get() = inMegaBytesDouble.toLong()
    inline val inGigaBytes: Long get() = inGigaBytesDouble.toLong()

    inline val inKiloBytesDouble: Double get() = inBytes.toDouble() / 1024.0
    inline val inMegaBytesDouble: Double get() = inKiloBytesDouble / 1024.0
    inline val inGigaBytesDouble: Double get() = inMegaBytesDouble / 1024.0

    inline operator fun times(another: Long): FileSize = FileSize(this.inBytes * another)
    inline operator fun div(another: Long): FileSize = FileSize(this.inBytes / another)
    inline operator fun plus(another: Long): FileSize = FileSize(this.inBytes + another)
    inline operator fun minus(another: Long): FileSize = FileSize(this.inBytes - another)

    inline operator fun times(another: FileSize): FileSize = FileSize(this.inBytes * another.inBytes)
    inline operator fun div(another: FileSize): FileSize = FileSize(this.inBytes / another.inBytes)
    inline operator fun plus(another: FileSize): FileSize = FileSize(this.inBytes + another.inBytes)
    inline operator fun minus(another: FileSize): FileSize = FileSize(this.inBytes - another.inBytes)

    inline operator fun times(another: Double): FileSize = FileSize((this.inBytes * another).toLong())
    inline operator fun div(another: Double): FileSize = FileSize((this.inBytes / another).toLong())
    inline operator fun plus(another: Double): FileSize = FileSize((this.inBytes + another).toLong())
    inline operator fun minus(another: Double): FileSize = FileSize((this.inBytes - another).toLong())

    inline operator fun times(another: Float): FileSize = FileSize((this.inBytes * another).toLong())
    inline operator fun div(another: Float): FileSize = FileSize((this.inBytes / another).toLong())
    inline operator fun plus(another: Float): FileSize = FileSize((this.inBytes + another).toLong())
    inline operator fun minus(another: Float): FileSize = FileSize((this.inBytes - another).toLong())

    inline operator fun times(another: Int): FileSize = FileSize(this.inBytes * another)
    inline operator fun div(another: Int): FileSize = FileSize(this.inBytes / another)
    inline operator fun plus(another: Int): FileSize = FileSize(this.inBytes + another)
    inline operator fun minus(another: Int): FileSize = FileSize(this.inBytes - another)

    companion object {
        inline val Long.bytes: FileSize get() = FileSize(this)
        inline val Long.kiloBytes: FileSize get() = this * 1024.bytes
        inline val Long.megaBytes: FileSize get() = (this * 1024).kiloBytes
        inline val Long.gigaBytes: FileSize get() = (this * 1024).megaBytes

        inline val Int.bytes: FileSize get() = toLongUnsigned().bytes
        inline val Int.kiloBytes: FileSize get() = this * 1024.bytes
        inline val Int.megaBytes: FileSize get() = (this * 1024).kiloBytes
        inline val Int.gigaBytes: FileSize get() = (this * 1024).megaBytes

        inline val Double.bytes: FileSize get() = this.toLong().bytes
        inline val Double.kiloBytes: FileSize get() = this * 1024.bytes
        inline val Double.megaBytes: FileSize get() = (this * 1024).kiloBytes
        inline val Double.gigaBytes: FileSize get() = (this * 1024).megaBytes

        val Zero = 0.bytes
        val Unspecified = FileSize(-1L)
    }

    @Suppress("DefaultLocale")
    override fun toString(): String {
        val gigaBytes = this.inGigaBytesDouble
        if (gigaBytes >= 1) {
            if (gigaBytes == this.inGigaBytes.toDouble()) {
                return "${gigaBytes.toLong()} GB"
            }
            return "${String.format("%.1f", gigaBytes)} GB"
        }
        val megaBytes = this.inMegaBytesDouble
        if (megaBytes >= 1) {
            if (megaBytes == this.inMegaBytes.toDouble()) {
                return "${megaBytes.toLong()} MB"
            }
            return "${String.format("%.1f", megaBytes)} MB"
        }
        val kiloBytes = this.inKiloBytesDouble
        if (kiloBytes >= 1) {
            if (kiloBytes == this.inKiloBytes.toDouble()) {
                return "${kiloBytes.toLong()} KB"
            }
            return "${String.format("%.1f", kiloBytes)} KB"
        }
        return "${this.inBytes} B"
    }
}

@PublishedApi
internal inline fun Int.toLongUnsigned(): Long = this.toLong().and(0xFFFF_FFFFL)

inline operator fun Int.times(another: FileSize): FileSize = FileSize(this.toLongUnsigned() * another.inBytes)
inline operator fun Int.div(another: FileSize): FileSize = FileSize(this.toLongUnsigned() / another.inBytes)
inline operator fun Int.plus(another: FileSize): FileSize = FileSize(this.toLongUnsigned() + another.inBytes)
inline operator fun Int.minus(another: FileSize): FileSize = FileSize(this.toLongUnsigned() - another.inBytes)

inline operator fun Long.times(another: FileSize): FileSize = FileSize(this * another.inBytes)
inline operator fun Long.div(another: FileSize): FileSize = FileSize(this / another.inBytes)
inline operator fun Long.plus(another: FileSize): FileSize = FileSize(this + another.inBytes)
inline operator fun Long.minus(another: FileSize): FileSize = FileSize(this - another.inBytes)

inline operator fun Double.times(another: FileSize): FileSize = FileSize((this * another.inBytes).toLong())
inline operator fun Double.div(another: FileSize): FileSize = FileSize((this / another.inBytes).toLong())
inline operator fun Double.plus(another: FileSize): FileSize = FileSize((this + another.inBytes).toLong())
inline operator fun Double.minus(another: FileSize): FileSize = FileSize((this - another.inBytes).toLong())

