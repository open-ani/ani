package me.him188.ani.utils.serialization

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlin.jvm.JvmName

@JvmName("toJsonArrayString")
fun List<String>.toJsonArray(): JsonArray {
    return buildJsonArray {
        for (element in this@toJsonArray) {
            add(JsonPrimitive(element))
        }
    }
}

@JvmName("toJsonArrayInt")
fun List<Int>.toJsonArray(): JsonArray {
    return buildJsonArray {
        for (element in this@toJsonArray) {
            add(JsonPrimitive(element))
        }
    }
}

@JvmName("toJsonArrayLong")
fun List<Long>.toJsonArray(): JsonArray {
    return buildJsonArray {
        for (element in this@toJsonArray) {
            add(JsonPrimitive(element))
        }
    }
}

@JvmName("toJsonArrayFloat")
fun List<Float>.toJsonArray(): JsonArray {
    return buildJsonArray {
        for (element in this@toJsonArray) {
            add(JsonPrimitive(element))
        }
    }
}

@JvmName("toJsonArrayDouble")
fun List<Double>.toJsonArray(): JsonArray {
    return buildJsonArray {
        for (element in this@toJsonArray) {
            add(JsonPrimitive(element))
        }
    }
}

@JvmName("toJsonArrayBoolean")
fun List<Boolean>.toJsonArray(): JsonArray {
    return buildJsonArray {
        for (element in this@toJsonArray) {
            add(JsonPrimitive(element))
        }
    }
}

fun JsonObjectBuilder.putAll(jsonObject: JsonObject) {
    for ((key, value) in jsonObject) {
        put(key, value)
    }
}
