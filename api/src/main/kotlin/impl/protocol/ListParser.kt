package me.him188.animationgarden.api.impl.protocol

import me.him188.animationgarden.api.impl.model.*
import me.him188.animationgarden.api.model.FileSize
import me.him188.animationgarden.api.model.FileSize.Companion.bytes
import me.him188.animationgarden.api.model.FileSize.Companion.gigaBytes
import me.him188.animationgarden.api.model.FileSize.Companion.kiloBytes
import me.him188.animationgarden.api.model.FileSize.Companion.megaBytes
import me.him188.animationgarden.api.model.MagnetLink
import me.him188.animationgarden.api.model.Topic
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class ParseException(
    override val message: String?,
    override val cause: Throwable? = null
) : Exception()

internal object ListParser {
    @JvmInline
    private value class Row(val elements: Elements) {
        val date: String get() = elements[0].child(0).text().trim()
        val categoryName: String get() = elements[1].child(0).text().trim()
        val categoryLink: String get() = elements[1].child(0).attr("href") // https://www.dmhy.org/topics/list/sort_id/2
        val allianceName: String? get() = elements[2].selectFirst("span.tag")?.select("a")?.text()?.trim()
        val allianceLink: String? get() = elements[2].select("span.tag").select("a").attr("href")
        val title: String
            get() {
                // alliance, title, comments count
                return elements[2].select("a").text().trim()
            }
        val commentsCount: String?
            get() {
                val element = elements[2]
                return if (allianceName.isNullOrEmpty()) {
                    if (element.childrenSize() == 2) {
                        element.child(1).text().trim()
                    } else {
                        null
                    }
                } else {
                    if (element.childrenSize() == 3) {
                        element.child(2).text().trim()
                    } else {
                        null
                    }
                }
            }
        val link: String get() = elements[2].select("a").last()!!.attr("href")
        val magnetLink: String get() = elements[3].child(0).attr("href")
        val size: String get() = elements[4].text()
        val authorName: String get() = elements.last()!!.text()
        val authorLink: String get() = elements.last()!!.select("a").attr("href")

        companion object {
            private val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        }

        fun toTopic(context: Cache): Topic {
            val id = link.substringAfterLast('/')
            val categoryId = categoryLink.substringAfterLast('/')
            val authorId = authorLink.substringAfterLast('/')
            val allianceId = allianceLink?.substringAfterLast('/')
            val allianceName = allianceName
            return TopicImpl(
                id = id,
                date = LocalDateTime.parse(date, formatter),
                category = context.categories.getOrSet(categoryId) { TopicCategoryImpl(categoryId, categoryName) },
                alliance = if (allianceId != null && allianceName != null)
                    context.alliances.getOrSet(allianceId) { AllianceImpl(allianceId, allianceName) }
                else null,
                title = title.removePrefix(allianceName ?: "").trimStart(),
                commentsCount = commentsCount?.filter { it.isDigit() }?.toIntOrNull() ?: 0,
                magnetLink = MagnetLink(magnetLink),
                size = createFileSize(size),
                author = context.users.getOrSet(authorId) { UserImpl(authorId, authorName) },
            )
        }

        private fun createFileSize(size: String): FileSize {
            // 430.1 MB
            return when {
                size.endsWith("GB") -> size.removeSuffix("GB").toDouble().gigaBytes
                size.endsWith("MB") -> size.removeSuffix("MB").toDouble().megaBytes
                size.endsWith("KB") -> size.removeSuffix("KB").toDouble().kiloBytes
                size.endsWith("B") -> size.removeSuffix("B").toDouble().bytes
                else -> throw IllegalArgumentException("Unrecognized size pattern: $size")
            }
        }
    }

    @Throws(ParseException::class)
    fun parseList(context: Cache, document: Document): List<Topic> {
        val tbody = document.select("table.tablesorter").singleOrNull()
            ?: throw ParseException("Cannot find table")
        return tbody.select("tbody").single().children().map { Row(it.children()).toTopic(context) }
    }
}