package me.him188.animationgarden.api.impl.protocol

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.him188.animationgarden.api.impl.model.Cache
import me.him188.animationgarden.api.impl.model.CacheImpl
import me.him188.animationgarden.api.model.Topic
import org.jsoup.nodes.Document

internal class Network(
    private val client: HttpClient
) {
    private object Paths {
        const val host: String = "www.dmhy.org"
        val userPathSegments: List<String> =
            listOf("topics", "list", "user_id")  // https://www.dmhy.org/topics/list/user_id/637871
        val alliancePathSegments: List<String> =
            listOf("topics", "list", "team_id") // https://www.dmhy.org/topics/list/team_id/801
    }

    data class ListResponse(
        val context: Cache,
        val list: List<Topic>,
        val currentPage: Int,
        val hasPreviousPage: Boolean,
        val hasNextPage: Boolean,
    )

    // https://www.dmhy.org/topics/list?keyword=lyc&sort_id=2&team_id=823&order=date-asc
    // page starts from 1
    suspend fun list(
        page: Int?,
        keyword: String?,
        sortId: String?,
        teamId: String?,
        orderId: String?
    ): ListResponse {
        require(page == null || page >= 1) { "page must be >= 1" }
        val resp = client.get {
            url {
                protocol = URLProtocol.HTTP
                host = Paths.host
                appendPathSegments("topics", "list")
                if (page != null && page != 1) {
                    appendPathSegments("page", page.toString())
                }
            }
            parameter("keyword", keyword)
            parameter("sort_id", sortId)
            parameter("team_id", teamId)
            parameter("order", orderId)
        }
        val document = resp.bodyAsDocument()
        val context = CacheImpl()
        return ListResponse(
            context = context,
            list = ListParser.parseList(context, document).orEmpty(),
            currentPage = 0,
            hasPreviousPage = false,
            hasNextPage = false
        )
    }
}

private suspend inline fun HttpResponse.bodyAsDocument(): Document = body()