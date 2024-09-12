package me.him188.ani.datasources.api.source.parameter

sealed interface MediaSourceParameter<T> {
    val name: String
    val description: String?  // todo: how to localize?
    val default: () -> T

    fun parseFromString(value: String): T
}

private val TrueValidator: (String) -> Boolean = { true }
private val NoopSanitizer: (String) -> String = { it }

class StringParameter(
    override val name: String,
    override val description: String? = null,
    override val default: () -> String,
    val placeholder: String? = null,
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
    override val default: () -> Boolean,
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
    override val default: () -> String,
) : MediaSourceParameter<String> {
    init {
        require(name.isNotEmpty()) { "name must not be empty" }
        require(oneOf.isNotEmpty()) { "oneOf must not be empty" }
    }

    override fun parseFromString(value: String): String {
        return value
    }
}
