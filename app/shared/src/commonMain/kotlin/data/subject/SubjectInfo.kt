package me.him188.ani.app.data.subject

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.openapitools.client.models.Subject

/**
 * 详细信息.
 */
@Immutable
@Serializable
class SubjectInfo(
    val id: Int = 0,
    // 可搜索 "吹响！悠风号 第三季.json" 看示例
    val name: String = "",
    val nameCn: String = "",
    val summary: String = "",
    val nsfw: Boolean = false,
    val locked: Boolean = false,
    /* TV, Web, 欧美剧, PS4... */
    val platform: String = "",
//    val images: Images,
    /* 书籍条目的册数，由旧服务端从wiki中解析 */
    val volumes: Int = 0,
    /* 由旧服务端从wiki中解析，对于书籍条目为`话数` */
    val eps: Int = 0,
    /* 数据库中的章节数量 */
    val totalEpisodes: Int = 0,
//    val rating: Rating,
    val tags: List<Tag> = emptyList(),
    /* air date in `YYYY-MM-DD` format */
    val date: String? = null,
    val infobox: List<InfoboxItem> = emptyList(),
    val imageCommon: String = "",
) {
    val publishDate: PackedDate = if (date == null) PackedDate.Invalid else PackedDate.parseFromDate(date)

    /**
     * 主要显示名称
     */
    val displayName: String get() = nameCn.takeIf { it.isNotBlank() } ?: name

    /**
     * 主中文名, 主日文名, 以及所有别名
     */
    val allNames by lazy(LazyThreadSafetyMode.PUBLICATION) {
        buildList {
            fun addIfNotBlank(name: String) {
                if (name.isNotBlank()) add(name)
            }
            addIfNotBlank(nameCn)
            addIfNotBlank(name)

            (infobox.firstOrNull { it.name == "别名" }?.value as? JsonArray)
                ?.forEach { element -> // interesting fact, 如果 `element` 改名成 `name`, 编译器就会编译错 (runtime class cast exception)
                    when (element) {
                        is JsonPrimitive -> addIfNotBlank(element.content)
                        is JsonObject -> (element["v"] as? JsonPrimitive)?.contentOrNull?.let { addIfNotBlank(it) }
                        else -> {}
                    }
                }
        }
    }

    companion object {
        @Stable
        @JvmStatic
        val Empty = SubjectInfo()
    }
}

@Stable
val SubjectInfo.nameCnOrName get() = nameCn.takeIf { it.isNotBlank() } ?: name


@Serializable
@Immutable
class Tag(
    val name: String,
    val count: Int,
)

@Serializable
@Immutable
class InfoboxItem(
    val name: String,
    val value: JsonElement,
)

fun Subject.createSubjectInfo(): SubjectInfo {
    return SubjectInfo(
        id = id,
        name = name,
        nameCn = nameCn,
        summary = this.summary,
        nsfw = this.nsfw,
        locked = this.locked,
        platform = this.platform,
        volumes = this.volumes,
        eps = this.eps,
        totalEpisodes = this.totalEpisodes,
        date = this.date,
        tags = this.tags.map { Tag(it.name, it.count) },
        infobox = this.infobox?.map { InfoboxItem(it.key, convertToJsonElement(it.value)) }.orEmpty(),
        imageCommon = this.images.common,
    )
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
            value.map { (k, v) -> k.toString() to convertToJsonElement(v) }.toMap()
        )

        else -> throw IllegalArgumentException("Unsupported type: ${value::class.java}")
    }
}

