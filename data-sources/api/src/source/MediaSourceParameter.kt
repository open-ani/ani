package me.him188.ani.datasources.api.source

class MediaSourceParameters(
    val list: List<MediaSourceParameter>,
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
class MediaSourceParametersBuilder {
    private val list = mutableListOf<MediaSourceParameter>()

    /**
     * 增加一个字符串参数
     */
    fun string(
        name: String,
        default: String? = null,
        description: String? = null,
    ) {
        add(StringParameter(name, description, default))
    }

    /**
     * 增加一个开关
     */
    fun boolean(
        name: String,
        default: Boolean,
        description: String? = null,
    ) {
        add(BooleanParameter(name, description, default))
    }

    /**
     * 增加一个枚举参数, 用户可从 [oneOf] 中选择一个. [oneOf] 必须至少有一个元素.
     */
    fun simpleEnum(
        name: String,
        oneOf: List<String>,
        default: String? = null,
        description: String? = null,
    ) {
        require(oneOf.isNotEmpty()) { "oneOf must not be empty" }
        if (default != null) {
            require(default in oneOf) { "default value must be in oneOf" }
        }
        add(SimpleEnumParameter(name, oneOf, description, default))
    }

    /**
     * 增加一个枚举参数, 用户可从 [oneOf] 中选择一个. [oneOf] 必须至少有一个元素.
     */
    fun simpleEnum(
        name: String,
        vararg oneOf: String,
        default: String? = null,
        description: String? = null,
    ) = simpleEnum(name, oneOf.toList(), description, default)

    fun add(parameter: MediaSourceParameter) {
        list.add(parameter)
    }

    fun build(): MediaSourceParameters = MediaSourceParameters(list)
}

sealed interface MediaSourceParameter

data class StringParameter(
    val name: String,
    val description: String? = null,
    val default: String? = null,
) : MediaSourceParameter

data class BooleanParameter(
    val name: String,
    val description: String? = null,
    val default: Boolean,
) : MediaSourceParameter

data class SimpleEnumParameter(
    val name: String,
    val oneOf: List<String>,
    val description: String? = null,
    val default: String? = null,
) : MediaSourceParameter 
