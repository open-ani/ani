package me.him188.ani.datasources.api.source

class MediaSourceParameters(
    val list: List<MediaSourceParameter<*>>,
) {
    companion object {
        val Empty = MediaSourceParameters(emptyList())
    }
}

inline fun buildMediaSourceParameters(block: MediaSourceParametersBuilder.() -> Unit): MediaSourceParameters =
    MediaSourceParametersBuilder().apply(block).build()

/**
 * @see buildMediaSourceParameters
 */
open class MediaSourceParametersBuilder {
    private val list = mutableListOf<MediaSourceParameter<*>>()

    /**
     * 增加一个字符串参数
     */
    fun string(
        name: String,
        default: String? = null,
        description: String? = null,
    ): StringParameter {
        val param = StringParameter(
            name, description,
            default = default ?: "",
            isRequired = default == null
        )
        add(param)
        return param
    }

    /**
     * 增加一个开关
     */
    fun boolean(
        name: String,
        default: Boolean,
        description: String? = null,
    ): BooleanParameter {
        val param = BooleanParameter(name, description, default)
        add(param)
        return param
    }

    /**
     * 增加一个枚举参数, 用户可从 [oneOf] 中选择一个. [oneOf] 必须至少有一个元素.
     */
    fun simpleEnum(
        name: String,
        oneOf: List<String>,
        default: String,
        description: String? = null,
    ): SimpleEnumParameter {
        val param = SimpleEnumParameter(name, oneOf, description, default)
        add(param)
        return param
    }

    /**
     * 增加一个枚举参数, 用户可从 [oneOf] 中选择一个. [oneOf] 必须至少有一个元素.
     */
    fun simpleEnum(
        name: String,
        vararg oneOf: String,
        default: String,
        description: String? = null,
    ) = simpleEnum(name, oneOf.toList(), default, description)

    fun <T> add(parameter: MediaSourceParameter<T>): MediaSourceParameter<T> {
        list.add(parameter)
        return parameter
    }

    fun build(): MediaSourceParameters = MediaSourceParameters(list)
}

sealed interface MediaSourceParameter<T> {
    val name: String
    val description: String?  // todo: how to localize?
    val default: T

    fun parseFromString(value: String): T
}

private val TrueValidator: (String) -> Boolean = { true }
private val NoopSanitizer: (String) -> String = { it }

class StringParameter(
    override val name: String,
    override val description: String? = null,
    override val default: String = "",
    val isRequired: Boolean = false,
    /**
     * 验证用户输入是否合法
     */
    validate: (String) -> Boolean = TrueValidator,
    /**
     * 用户每输入一个字都会用整个编辑框的值调用这个函数, 可用于自动清除首尾空格等
     */
    val sanitize: (String) -> String = NoopSanitizer,
) : MediaSourceParameter<String> {
    val validate: (String) -> Boolean = {
        if (isRequired && it.isBlank()) {
            false
        } else {
            validate(it)
        }
    }

    init {
        require(name.isNotEmpty()) { "name must not be empty" }
    }

    override fun parseFromString(value: String): String {
        return sanitize(value)
    }
}

data class BooleanParameter(
    override val name: String,
    override val description: String? = null,
    override val default: Boolean,
) : MediaSourceParameter<Boolean> {
    init {
        require(name.isNotEmpty()) { "name must not be empty" }
    }

    override fun parseFromString(value: String): Boolean {
        return value.toBoolean()
    }
}

data class SimpleEnumParameter(
    override val name: String,
    val oneOf: List<String>,
    override val description: String? = null,
    override val default: String,
) : MediaSourceParameter<String> {
    init {
        require(name.isNotEmpty()) { "name must not be empty" }
        require(oneOf.isNotEmpty()) { "oneOf must not be empty" }
        require(default in oneOf) { "default value must be in oneOf" }
    }

    override fun parseFromString(value: String): String {
        return value
    }
}
