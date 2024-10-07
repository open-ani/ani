/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package org.intellij.lang.annotations // 必须和 IJ 相同

/**
 * @sample org.intellij.lang.annotations.Language
 */
@OptIn(ExperimentalMultiplatform::class)
@Retention(AnnotationRetention.SOURCE) // 我们不分发, 有 SOURCE 就够了
@OptionalExpectation
expect annotation class Language(
    val value: String,
    val prefix: String = "",
    val suffix: String = ""
) // no actual on any platform. 实际上 JVM 有这个类, 所以如果这里 JVM actual, 会有冲突
