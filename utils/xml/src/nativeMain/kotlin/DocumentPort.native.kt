/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

@file:Suppress(
    "NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS", "ACTUAL_WITHOUT_EXPECT", "EXPECT_ACTUAL_INCOMPATIBILITY",
)

package me.him188.ani.utils.xml

actual typealias Document = com.fleeksoft.ksoup.nodes.Document
actual typealias Node = com.fleeksoft.ksoup.nodes.Node
actual typealias Element = com.fleeksoft.ksoup.nodes.Element
actual typealias Elements = com.fleeksoft.ksoup.select.Elements
actual typealias Evaluator = com.fleeksoft.ksoup.select.Evaluator
