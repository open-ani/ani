import kotlinx.serialization.json.Json
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.ikaros.models.IkarosSubjectMeta
import me.him188.ani.datasources.ikaros.models.IkarosSubjectType
import models.IkarosPagingWrap
import org.apache.http.HttpHeaders
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.function.Function

class IkarosClient(private val baseUrl: String, private val username: String, private val password: String) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(IkarosClient::class.java)
    }
    
    private val client: HttpClient = HttpClients.createDefault()
    private var authStr = "Basic "

    init {
        authStr +=
            Base64.getEncoder().encodeToString("$username:$password".toByteArray(StandardCharsets.UTF_8))
    }

    fun checkConnection(): Int {
        val get = HttpGet(baseUrl)
        try {
            return client.execute(get).statusLine.statusCode
        } catch (e: IOException) {
            logger.error("Check connection failed", e)
            return HttpStatus.SC_SERVICE_UNAVAILABLE
        }
    }

    fun getSubjectMediaMatchs(query: MediaFetchRequest?): IkarosPagingWrap<MediaMatch> {
        val subjectNameCN = query?.subjectNameCN
        val subjectMetas = getSubjectMetas(subjectNameCN, 1, 15)
        for (subjectMeta in subjectMetas.items) {
//            var media:Media = DefaultMedia(
//                mediaId = subjectMeta.id.toString(),
//                mediaSourceId = IkarosMediaSource.ID,
//                originalTitle = subjectMeta.name.toString(),
//                download = "",
//                originalUrl = "",
//                properties = MediaProperties(),
//                
//            );
//            var mediaMatch:MediaMatch = MediaMatch(media, MatchKind.FUZZY);
        }
        return IkarosPagingWrap.emptyResult()
    }

    fun getSubjectMetas(name: String?, page: Int = 1, size: Int = 15): IkarosPagingWrap<IkarosSubjectMeta> {
        if (name == null || name.isEmpty()) {
            return IkarosPagingWrap.emptyResult()
        }
        val nameCnEncoding = Base64.getEncoder().encodeToString(name.toByteArray(StandardCharsets.UTF_8))
        val get = HttpGet(
            baseUrl + "/api/v1alpha1/subjects/condition"
                    + "?page=" + page + "&size=" + size + "&name=" + nameCnEncoding 
                    + "&type=" + IkarosSubjectType.ANIME.name
        )
        get.addHeader(HttpHeaders.AUTHORIZATION, authStr)

        val httpResponse: HttpResponse
        val json: String
        try {
            httpResponse = client.execute(get)
            val entity = httpResponse.entity
            val bytes = entity.content.readAllBytes()
            json = String(bytes, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            logger.error("Get Ikaros SubjectMetas failed", e)
            return IkarosPagingWrap.emptyResult()
        }
        if (httpResponse.statusLine.statusCode != HttpStatus.SC_OK) {
            logger.error(
                "Get Ikaros SubjectMetas failed for http status code: {} and message: {}",
                httpResponse.statusLine.statusCode,
                httpResponse.statusLine.reasonPhrase
            )
            return IkarosPagingWrap.emptyResult()
        }

        val pagingWrap:IkarosPagingWrap<IkarosSubjectMeta> = Json.decodeFromString(json);
        
        return pagingWrap
    }
    
    
}
