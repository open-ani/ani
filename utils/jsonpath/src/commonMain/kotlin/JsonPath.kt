/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.utils.jsonpath

import com.nfeld.jsonpathkt.kotlinx.resolveAsStringOrNull
import com.nfeld.jsonpathkt.kotlinx.resolveOrNull
import kotlinx.serialization.json.JsonElement
import org.intellij.lang.annotations.Language

typealias JsonPath = com.nfeld.jsonpathkt.JsonPath


//@JvmInline
//value class JsonPath(
//    @PublishedApi internal val delegate: LibJsonPath,
//) {
//    companion object {
//        @JvmStatic
//        fun compile(@Language("jsonpath") expression: String): JsonPath = JsonPath(LibJsonPath.compile(expression))
//
//        @JvmStatic
//        fun compileOrNull(@Language("jsonpath") expression: String): JsonPath? = try {
//            JsonPath(LibJsonPath.compile(expression))
//        } catch (e: Exception) {
//            null
//        }
//    }
//}

fun com.nfeld.jsonpathkt.JsonPath.Companion.compileOrNull(
    @Language("jsonpath") expression: String
): JsonPath? = try {
    compile(expression)
} catch (e: Exception) {
    null
}

fun JsonElement.resolveOrNull(path: JsonPath): JsonElement? = this.resolveOrNull(path)
fun JsonElement.resolveAsStringOrNull(path: JsonPath): String? = this.resolveAsStringOrNull(path)
