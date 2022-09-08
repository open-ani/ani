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
    constructor(time: Long, random: Long) : this(
        time.toString() + random.coerceAtLeast(1e7.toLong()).toString().takeLast(8)
    )

    val time: Long get() = value.dropLast(8).toLong()
    val random: Long get() = value.takeLast(8).toLong()

    override fun toString(): String = value

    companion object {
        fun generate(time: Long = System.currentTimeMillis(), random: Random = Random): CommitRef {
            return CommitRef(time, random.nextLong(1e7.toLong() until 1e8.toLong()))
        }
    }
}
