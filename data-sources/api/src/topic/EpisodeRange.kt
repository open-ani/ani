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

package me.him188.ani.datasources.api.topic

import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.EpisodeSort

/**
 * single or range, parsed from topic titles
 */
sealed class EpisodeRange {
    abstract val sorts: Sequence<EpisodeSort>

    private data object Empty : EpisodeRange() {
        override val sorts: Sequence<EpisodeSort>
            get() = emptySequence()

        override fun toString(): String = "EpisodeRange(empty)"
    }

    private class Single(
        val value: EpisodeSort,
    ) : EpisodeRange() {
        override val sorts: Sequence<EpisodeSort>
            get() = sequenceOf(value)

        override fun toString(): String = "$value..$value"

        override fun hashCode(): Int = value.hashCode()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other is Range && other.start == other.end) {
                return value == other.start
            }
            if (javaClass != other?.javaClass) return false
            other as Single
            return value == other.value
        }
    }

    private class Range(
        val start: EpisodeSort,
        val end: EpisodeSort,
    ) : EpisodeRange() {
        override val sorts: Sequence<EpisodeSort>
            get() = sequence {
                if (!(start is EpisodeSort.Normal && end is EpisodeSort.Normal)) {
                    yield(start)
                    yield(end)
                    return@sequence
                }
                var curr = start.number
                if (start.isPartial) {
                    yield(start)
                    curr += 0.5f
                }
                while (curr < end.number) {
                    yield(EpisodeSort.Normal(curr))
                    curr += 1f
                }
                yield(EpisodeSort.Normal(end.number))
            }

        override fun toString(): String = "$start..$end"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other is Single && other.value == start && other.value == end) return true
            if (javaClass != other?.javaClass) return false
            other as Range
            return start == other.start && end == other.end
        }

        override fun hashCode(): Int {
            var result = start.hashCode()
            result = 31 * result + end.hashCode()
            return result
        }
    }

    companion object {
        fun empty(): EpisodeRange = Empty
        fun single(raw: EpisodeSort): EpisodeRange = Single(raw)
        fun single(raw: String): EpisodeRange = Single(EpisodeSort(raw))
        fun range(start: EpisodeSort, end: EpisodeSort): EpisodeRange = Range(start, end)
        fun range(start: String, end: String) = range(EpisodeSort(start), EpisodeSort(end))
        fun range(start: Int, end: Int) = range(EpisodeSort(start), EpisodeSort(end))
    }
}

operator fun EpisodeRange.contains(expected: EpisodeSort): Boolean {
    return sorts.any { it == expected } // TODO: optimize  EpisodeRange.contains
}

@Serializable
data class Alliance(
    val id: String,
    val name: String,
)
