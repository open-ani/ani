/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.api.protocol

import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.random.nextLong

@Serializable
@JvmInline
value class CommitRef(val value: String) {
    init {
        check(value.length > 9) { "Invalid CommitRef: $value" }
    }

    constructor(base: Long, increment: Long) : this(
        base.coerceAtLeast(1e7.toLong()).toString().takeLast(8) + '0' + increment
    )

    // 70402761    0      1
    // base      const  increment

    val base: Long get() = baseStr.toLong()
    val baseStr: String get() = value.take(8)

    val const: Int get() = value[8].digitToInt()

    val increment: Long get() = value.drop(9).toLong()

    override fun toString(): String = value

    companion object {
        fun generate(random: Random = Random, increment: Long = 0): CommitRef {
            return CommitRef(random.nextLong(1e7.toLong() until 1e8.toLong()), increment)
        }
    }
}

fun CommitRef.isParentOf(ref: CommitRef): Boolean {
    if (this.baseStr != ref.baseStr) return false
    return this.value < ref.value
}

fun CommitRef.next(): CommitRef = CommitRef(base, increment.inc())
