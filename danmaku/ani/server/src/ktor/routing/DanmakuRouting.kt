package me.him188.ani.danmaku.server.ktor.routing

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import me.him188.ani.danmaku.protocol.Danmaku
import me.him188.ani.danmaku.protocol.DanmakuGetResponse
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.danmaku.protocol.DanmakuLocation
import me.him188.ani.danmaku.protocol.DanmakuPostRequest
import me.him188.ani.danmaku.server.service.DanmakuService
import me.him188.ani.danmaku.server.util.exception.AcquiringTooMuchDanmakusException
import me.him188.ani.danmaku.server.util.exception.BadRequestException
import me.him188.ani.danmaku.server.util.exception.EmptyDanmakuException
import me.him188.ani.danmaku.server.util.exception.fromException
import me.him188.ani.danmaku.server.util.getUserIdOrRespond
import org.koin.ktor.ext.inject
import java.awt.Color

fun Route.danmakuRouting() {
    val service: DanmakuService by inject()

    route("/danmaku/{episodeId}") {
        authenticate("auth-jwt") {
            postDocumentation()
            post {
                val userId = getUserIdOrRespond() ?: return@post
                val request = call.receive<DanmakuPostRequest>()
                val episodeId = call.parameters["episodeId"] ?: throw BadRequestException("Missing parameter episodeId")
                service.postDanmaku(episodeId, request.danmakuInfo, userId)
                call.respond(HttpStatusCode.OK)
            }
        }

        getDocumentation()
        get {
            val maxCount = call.request.queryParameters["maxCount"]?.toIntOrNull()
            val fromTime = call.request.queryParameters["fromTime"]?.toLongOrNull()
            val toTime = call.request.queryParameters["toTime"]?.toLongOrNull()
            val episodeId = call.parameters["episodeId"] ?: throw BadRequestException("Missing parameter episodeId")
            val result = service.getDanmaku(episodeId, maxCount, fromTime, toTime)
            call.respond(DanmakuGetResponse(result))
        }
    }
}

private fun Route.postDocumentation() {
    install(NotarizedRoute()) {
        post = PostInfo.builder {
            summary("发送弹幕")
            description("发送一条弹幕至某一剧集，可指定弹幕时间、内容、颜色和内容。需要用户登录。")
            parameters(
                Parameter(
                    name = "episodeId",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING,
                    description = "剧集ID", 
                    required = true
                ),
            )
            request {
                requestType<DanmakuPostRequest>()
                description("弹幕信息")
                examples(
                    "" to DanmakuPostRequest(
                        DanmakuInfo(
                            0,
                            Color.BLACK.rgb,
                            "Hello, world!",
                            DanmakuLocation.NORMAL
                        )
                    )
                )
            }
            response {
                responseCode(HttpStatusCode.OK)
                responseType<Any>()
                description("弹幕发送成功")
            }
            canRespond {
                responseCode(HttpStatusCode.BadRequest)
                responseType<Any>()
                description("请求参数错误")
            }
            canRespond {
                responseCode(HttpStatusCode.Unauthorized)
                responseType<Any>()
                description("未登录或用户token无效")
            }
            canRespond {
                responseCode(HttpStatusCode.fromException(EmptyDanmakuException()))
                responseType<Any>()
                description("弹幕内容为空")
            }
        }
    }
}

private fun Route.getDocumentation() {
    install(NotarizedRoute()) {
        get = GetInfo.builder {
            summary("获取弹幕")
            description("获取某一剧集内的弹幕，可指定某一时间范围及最大获取数量。")
            parameters(
                Parameter(
                    name = "episodeId",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING,
                    description = "剧集ID",
                    required = true
                ),
                Parameter(
                    name = "maxCount",
                    `in` = Parameter.Location.query,
                    schema = TypeDefinition.INT,
                    description = "最大弹幕获取数量，默认为8000",
                    required = false
                ),
                Parameter(
                    name = "fromTime",
                    `in` = Parameter.Location.query,
                    schema = TypeDefinition.LONG,
                    description = "过滤范围开始时间，单位为毫秒，默认为0",
                    required = false
                ),
                Parameter(
                    name = "toTime",
                    `in` = Parameter.Location.query,
                    schema = TypeDefinition.LONG,
                    description = "过滤范围结束时间，单位为毫秒，默认为-1；值为负数时表示不限制结束时间",
                    required = false
                ),
            )
            response {
                responseCode(HttpStatusCode.OK)
                responseType<DanmakuGetResponse>()
                description("弹幕列表")
                examples("" to DanmakuGetResponse(listOf(
                    Danmaku(
                        "ba1f213a-50bd-4e09-a4e0-de6e24b72e22",
                        "3db414d0-930a-4144-84cf-b841f486215e",
                        DanmakuInfo(
                            0,
                            Color.BLACK.rgb,
                            "Hello, world!",
                            DanmakuLocation.NORMAL
                        )
                    )
                )))
            }
            canRespond {
                responseCode(HttpStatusCode.BadRequest)
                responseType<Any>()
                description("请求参数错误")
            }
            canRespond {
                responseCode(HttpStatusCode.fromException(AcquiringTooMuchDanmakusException()))
                responseType<Any>()
                description("请求弹幕数量过多。maxCount参数传入值超过8000时会返回此错误。")
            }
        }
    }
}
