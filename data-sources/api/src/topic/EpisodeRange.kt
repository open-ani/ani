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
import me.him188.ani.datasources.api.topic.EpisodeRange.Combined
import me.him188.ani.datasources.api.topic.EpisodeRange.Range
import me.him188.ani.datasources.api.topic.EpisodeRange.Season
import me.him188.ani.datasources.api.topic.EpisodeRange.Single

/**
 * 剧集范围:
 * - [Single] 单个剧集
 * - [Range] 一段连续剧集范围
 * - [Combined] 多个 [EpisodeRange] 的组合, 不连续
 * - [Season] 一整季的剧集, 但是不知道具体包含哪些集数, 也可能不知道具体是哪一季
 */
@Serializable
sealed class EpisodeRange {
    /**
     * 是否知道具体集数
     */
    open val isKnown: Boolean get() = true

    /**
     * 已知的集数列表. 若未知, 则返回空序列
     */
    abstract val knownSorts: Sequence<EpisodeSort>

    @Serializable
    private data object Empty : EpisodeRange() {
        override val knownSorts: Sequence<EpisodeSort>
            get() = emptySequence()

        override fun toString(): String = "EpisodeRange(empty)"
    }

    @Serializable
    internal class Single(
        val value: EpisodeSort,
    ) : EpisodeRange() {
        override val knownSorts: Sequence<EpisodeSort>
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

    @Serializable
    internal class Range(
        val start: EpisodeSort,
        val end: EpisodeSort,
    ) : EpisodeRange() {
        override val knownSorts: Sequence<EpisodeSort>
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

    @Serializable
    class Combined(
        val first: EpisodeRange,
        val second: EpisodeRange,
    ) : EpisodeRange() {
        override val knownSorts: Sequence<EpisodeSort>
            get() = sequence {
                yieldAll(first.knownSorts)
                yieldAll(second.knownSorts)
            }

        override fun toString(): String = when {
            second is Single -> "$first+${second.value}"
            first is Single -> "${first.value}+$second"
            else -> "$first+$second"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is EpisodeRange) return false
            return knownSorts.toList() == other.knownSorts.toList() // TODO: optimize performance EpisodeRange.Combined.equals
        }

        override fun hashCode(): Int {
            var result = first.hashCode()
            result = 31 * result + second.hashCode()
            return result
        }
    }

    /**
     * 季度全集, 但是不知道具体包含哪些集数
     */
    @Serializable
    data class Season(
        /**
         * 第几季
         */
        val rawNumber: Int,
    ) : EpisodeRange() {
        val numberOrZero: Int get() = if (rawNumber == -1) 0 else rawNumber
        val numberOrNull: Int? get() = if (rawNumber == -1) null else rawNumber

        override val knownSorts: Sequence<EpisodeSort> get() = emptySequence()
        override val isKnown: Boolean get() = false
        override fun toString(): String = if (rawNumber != -1) "S$rawNumber" else "S?"
    }

    companion object {
        fun empty(): EpisodeRange = Empty
        fun single(raw: EpisodeSort): EpisodeRange = Single(raw)
        fun single(raw: String): EpisodeRange = Single(EpisodeSort(raw))
        fun range(start: EpisodeSort, end: EpisodeSort): EpisodeRange = Range(start, end)
        fun range(start: String, end: String) = range(EpisodeSort(start), EpisodeSort(end))
        fun range(start: Int, end: Int) = range(EpisodeSort(start), EpisodeSort(end))
        fun combined(first: EpisodeRange, second: EpisodeRange) = Combined(first, second)
        fun range(episodes: Iterable<EpisodeSort>): EpisodeRange =
            combined(episodes.map { single(it) })

        fun combined(list: Iterable<EpisodeRange>): EpisodeRange =
            list.reduceOrNull { acc, episodeRange -> combined(acc, episodeRange) }
                ?: Empty

        fun season(number: Int): Season = Season(number)

        @JvmName("seasonNullable")
        fun season(number: Int?): Season = Season(number ?: -1)
        fun unknownSeason(): Season = Season(-1)
    }
}

operator fun EpisodeRange.plus(other: EpisodeRange): EpisodeRange = EpisodeRange.combined(this, other)

operator fun EpisodeRange.contains(expected: EpisodeSort): Boolean = contains(expected, allowSeason = true)

fun EpisodeRange.contains(expected: EpisodeSort, allowSeason: Boolean = true): Boolean {
    if (allowSeason && this is Season) return true
    return knownSorts.any { it == expected } // TODO: optimize  EpisodeRange.contains
}

/**
 * 是否为单一剧集. 季度全集不算.
 */
fun EpisodeRange.isSingleEpisode(): Boolean {
    return when (this) {
        is Single -> true
        is Range -> start == end
        is Combined -> first == second
        else -> false
    }
}

fun EpisodeRange.hasSeason(): Boolean = when (this) {
    is Season -> true
    is Combined -> first.hasSeason() || second.hasSeason()
    else -> false
}

@Serializable
data class Alliance(
    val id: String,
    val name: String,
)
