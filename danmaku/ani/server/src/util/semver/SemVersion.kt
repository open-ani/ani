/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

/*
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 */

package me.him188.ani.danmaku.server.util.semver

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.LazyThreadSafetyMode.PUBLICATION
import me.him188.ani.danmaku.server.util.semver.ResolveContext.Kind.SEMANTIC_VERSION
import me.him188.ani.danmaku.server.util.semver.ResolveContext.Kind.VERSION_REQUIREMENT

/**
 * [语义化版本](https://semver.org/lang/zh-CN/) 支持
 *
 * ### 解析示例
 *
 * `1.0.0-M4+c25733b8` 将会解析出下面的内容,
 * [major] (主本号), [minor] (次版本号), [patch] (修订号), [identifier] (先行版本号) 和 [metadata] (元数据).
 * ```
 * SemVersion(
 *   major = 1,
 *   minor = 0,
 *   patch = 0,
 *   identifier  = "M4"
 *   metadata    = "c25733b8"
 * )
 * ```
 * 其中 identifier 和 metadata 都是可选的.
 *
 * 对于核心版本号, 此实现稍微比语义化版本规范宽松一些, 允许 x.y 的存在.
 *
 * ### 序列化
 * 使用 [SemVersionAsStringSerializer], [SemVersion] 被序列化为 [toString] 的字符串.
 *
 * @see Requirement 版本号要修
 * @see SemVersion.invoke 由字符串解析
 */
@Serializable(with = SemVersion.SemVersionAsStringSerializer::class)
public data class SemVersion
/**
 * @see SemVersion.invoke 字符串解析
 */
internal constructor(
    /** 主版本号 */
    public val major: Int,
    /** 次版本号 */
    public val minor: Int,
    /** 修订号 */
    public val patch: Int?,
    /** 先行版本号识别符 */
    public val identifier: String? = null,
    /** 版本号元数据, 不参与版本号对比([compareTo]), 但是参与版本号严格对比([equals]) */
    public val metadata: String? = null,
) : Comparable<SemVersion> {

    init {
        require(major >= 0) { "major must >= 0" }
        require(minor >= 0) { "minor must >= 0" }
        if (patch != null) require(patch >= 0) { "patch must >= 0" }
        if (identifier != null) require(identifier.none(Char::isWhitespace)) { "identifier must not contain whitespace" }
        if (metadata != null) require(metadata.none(Char::isWhitespace)) { "metadata must not contain whitespace" }
    }

    /**
     * 一条依赖规则
     * @see [parseRangeRequirement]
     */
    @Serializable(Requirement.RequirementAsStringSerializer::class)
    public data class Requirement internal constructor(
        /**
         * 规则的字符串表示方式
         *
         * @see [SemVersion.parseRangeRequirement]
         */
        val rule: String,
    ) {

        @Transient
        internal val impl = kotlin.runCatching {
            SemVersionInternal.parseRangeRequirement(rule)
        }.getOrElse {
            throw java.lang.IllegalArgumentException("Syntax error: $rule", it)
        }

        /** 在 [version] 满足此要求时返回 true */
        public fun test(version: SemVersion): Boolean = impl.test(version)

        /**
         * 序列化为字符串, [rule]. 从字符串反序列化, [parseRangeRequirement].
         */
        public object RequirementAsStringSerializer : KSerializer<Requirement> by String.serializer().map(
            serializer = { it.rule },
            deserializer = { parseRangeRequirement(it) }
        )

        public companion object {
            /**
             * @see parseRangeRequirement
             */
            @JvmSynthetic
            public operator fun invoke(@ResolveContext(VERSION_REQUIREMENT) requirement: String): Requirement =
                parseRangeRequirement(requirement)
        }
    }

    /**
     * 使用 [SemVersion.toString] 序列化, 使用 [SemVersion.invoke] 反序列化.
     */
    public object SemVersionAsStringSerializer : KSerializer<SemVersion> by String.serializer().map(
        serializer = { it.toString() },
        deserializer = { SemVersion(it) }
    )

    public companion object {
        /**
         * 解析一个版本号, 将会返回一个 [SemVersion],
         * 如果发生解析错误将会抛出一个 [IllegalArgumentException] 或者 [NumberFormatException]
         *
         * 对于版本号的组成, 有以下规定:
         * - 必须包含主版本号和次版本号
         * - 存在 先行版本号 的时候 先行版本号 不能为空
         * - 存在 元数据 的时候 元数据 不能为空
         * - 核心版本号只允许 `x.y` 和 `x.y.z` 的存在
         *     - `1.0-RC` 是合法的
         *     - `1.0.0-RC` 也是合法的, 与 `1.0-RC` 一样
         *     - `1.0.0.0-RC` 是不合法的, 将会抛出一个 [IllegalArgumentException]
         *
         * 注意情况:
         * - 第一个 `+` 之后的所有内容全部识别为元数据
         *     - `1.0+METADATA-M4`, metadata="METADATA-M4"
         * - 如果不确定版本号是否合法, 可以使用 [regex101.com](https://regex101.com/r/vkijKf/1/) 进行检查
         *     - 此实现使用的正则表达式为 `^(0|[1-9]\d*)\.(0|[1-9]\d*)(?:\.(0|[1-9]\d*))?(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$`
         */
        @JvmStatic
        @JvmName("parse")
        @Throws(IllegalArgumentException::class, NumberFormatException::class)
        public operator fun invoke(@ResolveContext(SEMANTIC_VERSION) version: String): SemVersion =
            SemVersionInternal.parse(version)

        /**
         * 解析一条依赖需求描述, 在无法解析的时候抛出 [IllegalArgumentException]
         *
         * 对于一条规则, 有以下方式可选
         *
         * - `1.0.0-M4`       要求 1.0.0-M4 版本, 且只能是 1.0.0-M4 版本
         * - `1.x`            要求 1.x 版本
         * - `> 1.0.0-RC`     要求 1.0.0-RC 之后的版本, 不能是 1.0.0-RC
         * - `>= 1.0.0-RC`    要求 1.0.0-RC 或之后的版本, 可以是 1.0.0-RC
         * - `< 1.0.0-RC`     要求 1.0.0-RC 之前的版本, 不能是 1.0.0-RC
         * - `<= 1.0.0-RC`    要求 1.0.0-RC 或之前的版本, 可以是 1.0.0-RC
         * - `!= 1.0.0-RC`    要求 除了1.0.0-RC 的任何版本
         *     - `[1.0.0, 1.2.0]`
         *     - `(1.0.0, 1.2.0]`
         *     - `[1.0.0, 1.2.0)`
         *     - `(1.0.0, 1.2.0)` [数学区间](https://baike.baidu.com/item/%E5%8C%BA%E9%97%B4/1273117)
         *
         * 对于多个规则, 允许使用逻辑符号 `{}`, `||`, `&&`
         * 例如:
         * - `1.x || 2.x || 3.0.0`
         * - `<= 0.5.3 || >= 1.0.0`
         * - `{> 1.0 && < 1.5} || {> 1.8}`
         * - `{> 1.0 && < 1.5} || {> 1.8}`
         * - `> 1.0.0 && != 1.2.0`
         *
         * 特别注意:
         * - 依赖规则版本号不需要携带版本号元数据, 元数据不参与依赖需求的检查
         * - 如果目标版本号携带有先行版本号, 请不要忘记先行版本号
         * - 因为 `()` 已经用于数学区间, 使用 `{}` 替代 `()`
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public fun parseRangeRequirement(@ResolveContext(VERSION_REQUIREMENT) rule: String): Requirement =
            Requirement(rule)

        /** @see [Requirement.test] */
        @JvmStatic
        @Throws(IllegalArgumentException::class, NumberFormatException::class)
        public fun Requirement.test(@ResolveContext(SEMANTIC_VERSION) version: String): Boolean = test(invoke(version))

        /**
         * 当满足 [requirement] 时返回 true, 否则返回 false
         */
        @JvmStatic
        public fun SemVersion.satisfies(requirement: Requirement): Boolean = requirement.test(this)

        /**
         * 当满足 [requirement] 时返回 true, 否则返回 false
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public fun SemVersion.satisfies(@ResolveContext(VERSION_REQUIREMENT) requirement: String): Boolean =
            parseRangeRequirement(requirement).test(this)

        /** for Kotlin only */
        @JvmStatic
        @JvmSynthetic
        public operator fun Requirement.contains(version: SemVersion): Boolean = test(version)

        /** for Kotlin only */
        @JvmStatic
        @JvmSynthetic
        public operator fun Requirement.contains(@ResolveContext(SEMANTIC_VERSION) version: String): Boolean =
            test(version)
    }

    @Transient
    private val toString: String by lazy(PUBLICATION) {
        buildString {
            append(major)
            append('.').append(minor)
            patch?.let { append('.').append(it) }
            identifier?.let { identifier ->
                append('-').append(identifier)
            }
            metadata?.let { metadata ->
                append('+').append(metadata)
            }
        }
    }

    /**
     * 返回类似 `1.0.0-M4+c25733b8` 的字符串.
     */
    public override fun toString(): String = toString

    /**
     * 将 [SemVersion] 转为 Kotlin data class 风格的 [String].
     *
     * ```
     * return "SemVersion(major=$major, minor=$minor, patch=$patch, identifier=$identifier, metadata=$metadata)"
     * ```
     */
    public fun toStructuredString(): String {
        return "SemVersion(major=$major, minor=$minor, patch=$patch, identifier=$identifier, metadata=$metadata)"
    }

    /**
     * 比较 `this` 和 [other].
     *
     * @param deep 为 `true` 时进行深度比较, 相当于 [equals]. 为 `false` 时相当于 `compareTo(other) == 0`
     * @see compareTo
     */
    public fun equals(other: SemVersion, deep: Boolean): Boolean {
        return if (deep) {
            (other.major == major
                    && other.minor == minor
                    && other.patch == patch
                    && other.identifier == identifier
                    && other.metadata == metadata)
        } else {
            this.compareTo(other) == 0
        }
    }

    /**
     * 深度比较 `this` 和 [other], 当且仅当 [major], [patch], [minor], [identifier], [metadata] 完全相同时返回 `true`.
     *
     * 如: `1.0.0-RC` != `1.0-RC`
     *
     * @see compareTo
     */
    public override fun equals(other: Any?): Boolean {
        if (other === null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false
        return equals(other as SemVersion, deep = true)
    }

    public override fun hashCode(): Int {
        var result = major.hashCode()
        result = 31 * result + minor.hashCode()
        result = 31 * result + (patch?.hashCode() ?: 0)
        result = 31 * result + (identifier?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        return result
    }

    /**
     * 比较 `this` 和 [other] 的实际版本大小.
     *
     * 如:
     * - `SemVersion("1.0.0-RC").compareTo(SemVersion("1.0-RC")) == 0` (然而对他们进行 [equals] 判断会返回 `false`)
     * - `SemVersion("1.3.0") > SemVersion("1.1.0") == true` (因为 1.3.0 比 1.1.0 更高)
     *
     *
     * @return 当 `this` 比 [other] 更高时返回一个正数.
     * 当 `this` 比 [other] 更低时返回一个负数.
     * 当 `this` 与 [other] 版本大小相等时返回 0.
     *
     * @see equals
     */
    public override operator fun compareTo(other: SemVersion): Int {
        return SemVersionInternal.run { compareInternal(this@SemVersion, other) }
    }
}

internal inline fun <E, R> KSerializer<E>.map(
    crossinline serializer: (R) -> E,
    crossinline deserializer: (E) -> R
): KSerializer<R> {
    return object : KSerializer<R> {
        override val descriptor: SerialDescriptor get() = this@map.descriptor
        override fun deserialize(decoder: Decoder): R = this@map.deserialize(decoder).let(deserializer)
        override fun serialize(encoder: Encoder, value: R) = this@map.serialize(encoder, value.let(serializer))
    }
}