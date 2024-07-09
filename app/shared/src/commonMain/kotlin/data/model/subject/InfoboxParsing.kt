package me.him188.ani.app.data.model.subject

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.him188.ani.datasources.bangumi.models.BangumiItem


val SubjectInfo.aliasSequence: Sequence<String>
    get() = infobox.asSequence()
        .filter { it.name == "别名" }
        .flatMap { box ->
            box.values
        }

private fun JsonElement.vSequence(): Sequence<String> {
    return when (this) {
        is JsonArray -> this.asSequence().flatMap { it.vSequence() }
        is JsonPrimitive -> sequenceOf(content)
        is JsonObject -> this["v"]?.vSequence() ?: emptySequence()
        else -> emptySequence()
    }
}

fun BangumiItem.toInfoboxItem(): InfoboxItem =
    InfoboxItem(this.key, this.value.vSequence().toList())
