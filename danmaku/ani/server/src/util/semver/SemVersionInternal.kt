/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package me.him188.ani.danmaku.server.util.semver

import util.semver.RequirementParser
import kotlin.math.max
import kotlin.math.min

@Suppress("RegExpRedundantEscape")
internal object SemVersionInternal {
    private val directVersion = """^[0-9]+(\.[0-9]+)+(|[\-+].+)$""".toRegex()
    private val versionSelect = """^[0-9]+(\.[0-9]+)*\.x$""".toRegex()
    private val versionMathRange =
        """([\[\(])([0-9]+(\.[0-9]+)+(|[\-+].+))\s*\,\s*([0-9]+(\.[0-9]+)+(|[\-+].+))([\]\)])""".toRegex()
    private val versionRule = """^((\>\=)|(\<\=)|(\=)|(\!\=)|(\>)|(\<))\s*([0-9]+(\.[0-9]+)+(|[\-+].+))$""".toRegex()

    private val SEM_VERSION_REGEX =
        """^(0|[1-9]\d*)\.(0|[1-9]\d*)(?:\.(0|[1-9]\d*))?(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$""".toRegex()

    /** 解析核心版本号, eg: `1.0.0` -> IntArray[1, 0, 0] */
    @JvmStatic
    private fun String.parseMainVersion(): IntArray =
        split('.').map { it.toInt() }.toIntArray()


    fun parse(version: String): SemVersion {
        if (!SEM_VERSION_REGEX.matches(version)) {
            throw IllegalArgumentException("`$version` not a valid version")
        }
        var mainVersionEnd = 0
        kotlin.run {
            val iterator = version.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next == '-' || next == '+') {
                    break
                }
                mainVersionEnd++
            }
        }
        var identifier: String? = null
        var metadata: String? = null
        if (mainVersionEnd != version.length) {
            when (version[mainVersionEnd]) {
                '-' -> {
                    val metadataSplitter = version.indexOf('+', startIndex = mainVersionEnd)
                    if (metadataSplitter == -1) {
                        identifier = version.substring(mainVersionEnd + 1)
                    } else {
                        identifier = version.substring(mainVersionEnd + 1, metadataSplitter)
                        metadata = version.substring(metadataSplitter + 1)
                    }
                }

                '+' -> {
                    metadata = version.substring(mainVersionEnd + 1)
                }
            }
        }
        val mainVersion = version.substring(0, mainVersionEnd).parseMainVersion()
        return SemVersion(
            major = mainVersion[0],
            minor = mainVersion[1],
            patch = mainVersion.getOrNull(2),
            identifier = identifier,
            metadata = metadata,
        )
    }

    @JvmStatic
    internal fun parseRule(rule: String): RequirementInternal {
        val trimmed = rule.trim()
        if (directVersion.matches(trimmed)) {
            val parsed = SemVersion.invoke(trimmed)
            return object : RequirementInternal {
                override fun test(version: SemVersion): Boolean = version.compareTo(parsed) == 0
            }
        }
        if (versionSelect.matches(trimmed)) {
            val regex = ("^" +
                    trimmed.replace(".", "\\.")
                        .replace("x", ".+") +
                    "$"
                    ).toRegex()
            return object : RequirementInternal {
                override fun test(version: SemVersion): Boolean = regex.matches(version.toString())
            }
        }
        versionMathRange.matchEntire(trimmed)?.let { range ->
            // 1 mode
            // 2 first
            // 5 sec
            // 8 type
            var typeStart = range.groupValues[1][0]
            var typeEnd = range.groupValues[8][0]
            var start = SemVersion.invoke(range.groupValues[2])
            var end = SemVersion.invoke(range.groupValues[5])
            if (start > end) {
                val c = end
                end = start
                start = c
                val x = typeEnd
                typeEnd = typeStart
                typeStart = x
            }
            val a: (SemVersion) -> Boolean = when (typeStart) {
                '[', ']' -> ({ start <= it })
                '(', ')' -> ({ start < it })
                else -> throw AssertionError()
            }
            val b: (SemVersion) -> Boolean = when (typeEnd) {
                '[', ']' -> ({ it <= end })
                '(', ')' -> ({ it < end })
                else -> throw AssertionError()
            }
            return object : RequirementInternal {
                override fun test(version: SemVersion): Boolean = a(version) && b(version)
            }
        }
        versionRule.matchEntire(trimmed)?.let { result ->
            val operator = result.groupValues[1]
            val version1 = SemVersion.invoke(result.groupValues[8])
            return when (operator) {
                ">=" -> {
                    object : RequirementInternal {
                        override fun test(version: SemVersion): Boolean = version >= version1
                    }
                }

                ">" -> {
                    object : RequirementInternal {
                        override fun test(version: SemVersion): Boolean = version > version1
                    }
                }

                "<=" -> {
                    object : RequirementInternal {
                        override fun test(version: SemVersion): Boolean = version <= version1
                    }
                }

                "<" -> {
                    object : RequirementInternal {
                        override fun test(version: SemVersion): Boolean = version < version1
                    }
                }

                "=" -> {
                    object : RequirementInternal {
                        override fun test(version: SemVersion): Boolean = version.compareTo(version1) == 0
                    }
                }

                "!=" -> {
                    object : RequirementInternal {
                        override fun test(version: SemVersion): Boolean = version.compareTo(version1) != 0
                    }
                }

                else -> error("operator=$operator, version=$version1")
            }
        }
        throw IllegalArgumentException("Cannot parse $rule")
    }


    @JvmStatic
    fun parseRangeRequirement(requirement: String): RequirementInternal =
        object : RequirementParser.ProcessorBase<RequirementInternal>() {
            override fun processLogic(isAnd: Boolean, chunks: Iterable<RequirementInternal>): RequirementInternal {
                return if (isAnd) object : RequirementInternal {
                    override fun test(version: SemVersion): Boolean {
                        return chunks.all { it.test(version) }
                    }
                } else object : RequirementInternal {
                    override fun test(version: SemVersion): Boolean {
                        return chunks.any { it.test(version) }
                    }
                }
            }

            override fun processString(
                reader: RequirementParser.TokenReader,
                token: RequirementParser.Token.Content
            ): RequirementInternal = kotlin.runCatching {
                parseRule(token.content)
            }.getOrElse { token.ia(reader, "Error in parsing rule `${token.content}`", it) }
        }.processLine(RequirementParser.TokenReader(requirement))

    @JvmStatic
    fun compareInternal(source: SemVersion, other: SemVersion): Int {
        // ignored metadata in comparing

        // If $this equals $other (without metadata),
        // return same.
        // Compare main-version

        source.major.compareTo(other.major).takeUnless { it == 0 }?.let { return it }
        source.minor.compareTo(other.minor).takeUnless { it == 0 }?.let { return it }
        (source.patch ?: 0).compareTo(other.patch ?: 0).takeUnless { it == 0 }?.let { return it }

        // If main-versions are same.
        var identifier0 = source.identifier
        var identifier1 = other.identifier
        // If anyone doesn't have the identifier...
        if (identifier0 == null || identifier1 == null) {
            return when (identifier0) {
                identifier1 -> { // null == null
                    // Nobody has identifier
                    0
                }

                null -> {
                    // $other has identifier, but $this don't have identifier
                    // E.g:
                    //   this    = 1.0.0
                    //   other   = 1.0.0-dev
                    1
                }
                // It is the opposite of the above.
                else -> -1
            }
        }
        fun String.getSafe(index: Int) = getOrElse(index) { ' ' }

        // ignored same prefix
        fun getSameSize(s1: String, s2: String): Int {
            val size = min(s1.length, s2.length)
            //   1.0-RC19  -> 19
            //   1.0-RC107 -> 107
            var realSameSize = 0
            for (index in 0 until size) {
                if (s1[index] != s2[index]) {
                    return realSameSize
                } else {
                    if (!s1[index].isDigit()) {
                        realSameSize = index + 1
                    }
                }
            }
            return realSameSize
        }

        // We ignore the same parts. Because we only care about the differences.
        // E.g:
        //  1.0-RC1 -> 1
        //  1.0-RC2 -> 2
        val ignoredSize = getSameSize(identifier0, identifier1)
        identifier0 = identifier0.substring(ignoredSize)
        identifier1 = identifier1.substring(ignoredSize)
        // Multi-chunk comparing
        val chunks0 = identifier0.split('-', '.')
        val chunks1 = identifier1.split('-', '.')
        chunkLoop@ for (index in 0 until (max(chunks0.size, chunks1.size))) {
            val value0 = chunks0.getOrNull(index)
            val value1 = chunks1.getOrNull(index)
            // Any chunk is null
            if (value0 == null || value1 == null) {
                // value0 == null && value1 == null is impossible
                return if (value0 == null) {
                    // E.g:
                    //  value0 = 1.0-RC-dev
                    //  value1 = 1.0-RC-dev-1
                    -1
                } else {
                    // E.g:
                    //  value0 = 1.0-RC-dev-1
                    //  value1 = 1.0-RC-dev
                    1
                }
            }
            try {
                val result = value0.toInt().compareTo(value1.toInt())
                if (result != 0) {
                    return result
                }
                continue@chunkLoop
            } catch (ignored: NumberFormatException) {
            }
            // compare chars
            for (index0 in 0 until (max(value0.length, value1.length))) {
                val result = value0.getSafe(index0).compareTo(value1.getSafe(index0))
                if (result != 0)
                    return result
            }
        }
        return 0
    }
}