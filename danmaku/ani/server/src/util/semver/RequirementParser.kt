/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package util.semver

import kotlin.math.max
import kotlin.math.min

internal class RequirementParser {
    sealed class Token {
        open var line: Int = -1
        open var pos: Int = -1
        open var sourcePos: Int = -1
        open lateinit var content: String

        sealed class GroupBod : Token() {
            class Left : GroupBod() {
                override var content: String
                    get() = "{"
                    set(_) {}
            }

            class Right : GroupBod() {
                override var content: String
                    get() = "}"
                    set(_) {}
            }
        }

        sealed class Logic : Token() {
            class And : Logic() {
                override var content: String
                    get() = "&&"
                    set(_) {}
            }

            class Or : Logic() {
                override var content: String
                    get() = "||"
                    set(_) {}
            }
        }

        class Content : Token()
        class Ending : Token() {
            override var content: String
                get() = ""
                set(_) {}
        }

        object Begin : Token() {
            override var content: String
                get() = ""
                set(_) {}
            override var line: Int
                get() = 0
                set(_) {}
            override var pos: Int
                get() = 0
                set(_) {}
            override var sourcePos: Int
                get() = 0
                set(_) {}
        }

        override fun toString(): String {
            return javaClass.canonicalName.substringAfterLast('.') + " - $content [$line, $pos]"
        }
    }

    companion object {
        const val END = '\u0000'
    }

    class TokenReader(
        @JvmField val content: String
    ) {
        @JvmField
        var pos: Int = 0

        @JvmField
        var line: Int = 0

        @JvmField
        var posi: Int = 0

        @JvmField
        var latestToken: Token = Token.Begin

        @JvmField
        var insertToken: Token? = Token.Begin
        fun peekChar(): Char {
            if (pos < content.length)
                return content[pos]
            return END
        }

        fun peekNextChar(): Char {
            if (pos + 1 < content.length)
                return content[pos + 1]
            return END
        }

        fun nextChar(): Char {
            val char = peekChar()
            pos++
            if (char == '\n') {
                line++
                posi = 0
            } else {
                posi++
            }
            return char
        }

        fun nextToken(): Token {
            insertToken?.let { insertToken = null; return it }
            return nextToken0().also { latestToken = it }
        }

        private fun nextToken0(): Token {
            if (pos < content.length) {
                while (peekChar().isWhitespace()) {
                    nextChar()
                }
                val startIndex = pos
                if (startIndex >= content.length) {
                    return Token.Ending().also {
                        it.line = line
                        it.pos = posi
                        it.sourcePos = content.length
                    }
                }
                val pline = line
                val ppos = posi
                nextChar()
                when (content[startIndex]) {
                    '&' -> {
                        if (peekChar() == '&') {
                            return Token.Logic.And().also {
                                it.pos = ppos
                                it.line = pline
                                it.sourcePos = startIndex
                                nextChar()
                            }
                        }
                    }

                    '|' -> {
                        if (peekChar() == '|') {
                            return Token.Logic.Or().also {
                                nextChar()
                                it.pos = ppos
                                it.line = pline
                                it.sourcePos = startIndex
                            }
                        }
                    }

                    '{' -> {
                        return Token.GroupBod.Left().also {
                            it.pos = ppos
                            it.line = pline
                            it.sourcePos = startIndex
                        }
                    }

                    '}' -> {
                        return Token.GroupBod.Right().also {
                            it.pos = ppos
                            it.line = pline
                            it.sourcePos = startIndex
                        }
                    }
                }
                while (true) {
                    when (val c = peekChar()) {
                        '&', '|' -> {
                            if (c == peekNextChar()) {
                                break
                            }
                            nextChar()
                        }

                        '{', '}' -> {
                            break
                        }

                        END -> break
                        else -> nextChar()
                    }
                }
                val endIndex = pos
                return Token.Content().also {
                    it.content = content.substring(startIndex, endIndex)
                    it.pos = ppos
                    it.line = pline
                    it.sourcePos = startIndex
                }
            }
            return Token.Ending().also {
                it.line = line
                it.pos = posi
                it.sourcePos = content.length
            }
        }
    }

    interface TokensProcessor<R> {
        fun process(reader: TokenReader): R
        fun processLine(reader: TokenReader): R
        fun processLogic(isAnd: Boolean, chunks: Iterable<R>): R
    }

    abstract class ProcessorBase<R> : TokensProcessor<R> {
        fun Token.ia(reader: TokenReader, msg: String, cause: Throwable? = null): Nothing {
            throw IllegalArgumentException("$msg (at [$line, $pos], ${cutSource(reader, sourcePos)})", cause)
        }

        fun cutSource(reader: TokenReader, index: Int): String {
            val content = reader.content
            val s = max(0, index - 10)
            val e = min(content.length, index + 10)
            return content.substring(s, e)
        }

        override fun process(reader: TokenReader): R {
            return when (val nextToken = reader.nextToken()) {
                is Token.Begin,
                is Token.GroupBod.Left -> {
                    val first = when (val next = reader.nextToken()) {
                        is Token.Content -> {
                            processString(reader, next)
                        }

                        is Token.GroupBod.Right -> {
                            nextToken.ia(
                                reader,
                                if (nextToken is Token.Begin)
                                    "Invalid token `}`"
                                else "The first token cannot be Group Ending",
                            )
                        }

                        is Token.Logic -> {
                            nextToken.ia(reader, "The first token cannot be Token.Logic")
                        }

                        is Token.Ending -> {
                            nextToken.ia(
                                reader,
                                if (nextToken is Token.Begin)
                                    "Requirement cannot be blank"
                                else "Except more tokens",
                            )
                        }

                        is Token.GroupBod.Left -> {
                            reader.insertToken = next
                            process(reader)
                        }

                        else -> {
                            next.ia(reader, "Bad token $next")
                        }
                    }
                    // null -> not set
                    // true -> AND mode
                    // false-> OR mode
                    var mode: Boolean? = null
                    val chunks = arrayListOf(first)
                    while (true) {
                        when (val next = reader.nextToken()) {
                            is Token.Ending,
                            is Token.GroupBod.Right -> {
                                val isEndingOfGroup = next is Token.GroupBod.Right
                                val isStartingOfGroup = nextToken is Token.GroupBod.Left
                                if (isStartingOfGroup != isEndingOfGroup) {
                                    fun getType(type: Boolean) = if (type) "`}`" else "<EOF>"
                                    next.ia(
                                        reader,
                                        "Except ${getType(isStartingOfGroup)} but got ${getType(isEndingOfGroup)}",
                                    )
                                } else {
                                    // reader.insertToken = next
                                    break
                                }
                            }

                            is Token.Logic -> {
                                val stx = next is Token.Logic.And
                                if (mode == null) mode = stx
                                else if (mode != stx) {
                                    fun getMode(type: Boolean) = if (type) "`&&`" else "`||`"
                                    next.ia(
                                        reader,
                                        "Cannot change logic mode after setting. " +
                                                "Except ${getMode(mode)} but got ${getMode(stx)}",
                                    )
                                }
                                chunks.add(process(reader))
                            }

                            else -> {
                                next.ia(
                                    reader,
                                    "Except ${
                                        when (mode) {
                                            null -> "`&&` or `||`"
                                            true -> "`&&`"
                                            false -> "`||`"
                                        }
                                    } but get `${next.content}`",
                                )
                            }
                        }
                    }
                    if (mode == null) {
                        first
                    } else {
                        processLogic(mode, chunks)
                    }
                }

                is Token.Content -> {
                    processString(reader, nextToken)
                }

                is Token.Ending -> {
                    nextToken.ia(reader, "Except more values.")
                }

                else -> {
                    nextToken.ia(reader, "Assert Error: $nextToken")
                }
            }
        }

        abstract fun processString(reader: TokenReader, token: Token.Content): R


        override fun processLine(reader: TokenReader): R {
            return process(reader).also {
                val tok = reader.nextToken()
                if (reader.nextToken() !is Token.Ending) {
                    tok.ia(reader, "Token reader stream not done")
                }
            }
        }
    }
}