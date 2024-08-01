/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package me.him188.ani.test

import kotlin.test.Test

@Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT")
actual typealias TestFactory = Test

actual open class DynamicTest(
    val displayName: String,
    val action: () -> Unit,
)

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual typealias DynamicTestsResult = Unit

actual fun dynamicTest(displayName: String, action: () -> Unit): DynamicTest = DynamicTest(displayName, action)

actual fun runDynamicTests(dynamicTests: List<DynamicTest>) {
    for (dynamicTest in dynamicTests) {
        println("=".repeat(32) + " ${dynamicTest.displayName} " + "=".repeat(32))
        dynamicTest.action.invoke()
        println("=".repeat(32) + "=".repeat(dynamicTest.displayName.length + 2) + "=".repeat(32))
    }
}
