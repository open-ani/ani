package me.him188.ani.app.data.subject

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.openapitools.client.models.Item


val SubjectInfo.aliasSequence: Sequence<String>
    get() = infobox.asSequence()
        .filter { it.name == "别名" }
        .flatMap { box ->
            box.value.vSequence()
        }

private fun JsonElement.vSequence(): Sequence<String> {
    return when (this) {
        is JsonArray -> this.asSequence().flatMap { it.vSequence() }
        is JsonPrimitive -> sequenceOf(content)
        is JsonObject -> this["v"]?.vSequence() ?: emptySequence()
        else -> emptySequence()
    }
}

private fun convertToJsonElement(value: Any?): JsonElement {
    return when (value) {
        null -> JsonNull
        is String -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is Array<*> -> JsonArray(value.map { it?.let { convertToJsonElement(it) } ?: JsonNull })
        is List<*> -> JsonArray(value.map { it?.let { convertToJsonElement(it) } ?: JsonNull })
        is Map<*, *> -> JsonObject(
            value.map { (k, v) -> k.toString() to convertToJsonElement(v) }.toMap(),
        )

        else -> throw IllegalArgumentException("Unsupported type: ${value::class.java}")
    }
}

fun Item.toInfoboxItem(): InfoboxItem = InfoboxItem(this.key, convertToJsonElement(this.value))
