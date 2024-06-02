package me.him188.ani.danmaku.server.ktor.routing

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.util.pipeline.PipelineContext
import me.him188.ani.danmaku.protocol.ReleaseClass
import me.him188.ani.danmaku.protocol.ReleaseUpdatesDetailedResponse
import me.him188.ani.danmaku.protocol.ReleaseUpdatesResponse
import me.him188.ani.danmaku.protocol.UpdateInfo
import me.him188.ani.danmaku.server.service.ClientReleaseInfoManager
import me.him188.ani.danmaku.server.service.ReleaseInfo
import me.him188.ani.danmaku.server.util.exception.BadRequestException
import me.him188.ani.danmaku.server.util.exception.InvalidClientVersionException
import me.him188.ani.danmaku.server.util.exception.fromException
import org.koin.ktor.ext.inject

fun Route.updatesRouting() {
    val clientReleaseInfoManager by inject<ClientReleaseInfoManager>()

    route("/updates") {
        route("/incremental") {
            incrementalDoc()
            get {
                val updates = updateInfos(clientReleaseInfoManager)
                call.respond(ReleaseUpdatesResponse(updates.map { it.version.toString() }))
            }
        }
        route("/incremental/details") {
            incrementalDetailedDoc()
            get {
                val updates = updateInfos(clientReleaseInfoManager)
                val clientPlatform = call.request.queryParameters["clientPlatform"] ?: throw BadRequestException()
                val clientArch = call.request.queryParameters["clientArch"] ?: throw BadRequestException()
                call.respond(ReleaseUpdatesDetailedResponse(updates.mapNotNull {
                    val downloadUrls = try {
                        clientReleaseInfoManager.parseDownloadUrls(it.version, "$clientPlatform-$clientArch")
                    } catch (e: IllegalArgumentException) {
                        return@mapNotNull null
                    }
                    UpdateInfo(
                        it.version.toString(),
                        downloadUrls,
                        it.publishTime,
                        it.description
                    )
                }))
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.updateInfos(
    clientReleaseInfoManager: ClientReleaseInfoManager
): List<ReleaseInfo> {
    val version = call.request.queryParameters["clientVersion"] ?: throw BadRequestException()
    val clientPlatform = call.request.queryParameters["clientPlatform"] ?: throw BadRequestException()
    val clientArch = call.request.queryParameters["clientArch"] ?: throw BadRequestException()
    val releaseClass = call.request.queryParameters["releaseClass"]?.let {
        ReleaseClass.fromStringOrNull(it)
    } ?: throw BadRequestException()

    val updates = clientReleaseInfoManager.getAllUpdateLogs(
        version,
        "$clientPlatform-$clientArch",
        releaseClass,
    )
    return updates
}

private fun Route.incrementalDoc() {
    install(NotarizedRoute()) {
        get = GetInfo.builder {
            summary("获取可更新的版本号列表")
            description("返回所有大于当前版本的更新版本号。")
            parameters(
                Parameter(
                    name = "clientVersion",
                    `in` = Parameter.Location.query,
                    required = true,
                    description = "客户端当前版本号。不合法的版本号会导致服务器返回461 Invalid Client Version错误。",
                    schema = TypeDefinition.STRING
                ),
                Parameter(
                    name = "clientPlatform",
                    `in` = Parameter.Location.query,
                    required = true,
                    description = "客户端平台，例：windows, android。不合法的值会导致服务器返回空的版本号列表。",
                    schema = TypeDefinition.STRING
                ),
                Parameter(
                    name = "clientArch",
                    `in` = Parameter.Location.query,
                    required = true,
                    description = "客户端架构，例：x86_64, arm64。不合法的值会导致服务器返回空的版本号列表。",
                    schema = TypeDefinition.STRING
                ),
                Parameter(
                    name = "releaseClass",
                    `in` = Parameter.Location.query,
                    required = true,
                    description = "更新版本的发布类型，可选值：alpha, beta, rc, stable。不合法的发布类型会导致服务器返回400 Bad Request错误。", 
                    schema = TypeDefinition.STRING
                )
            )
            response {
                responseCode(HttpStatusCode.OK)
                responseType<ReleaseUpdatesResponse>()
                description("更新版本号列表")
                examples(
                    "" to ReleaseUpdatesResponse(listOf("3.0.0-rc01", "3.0.0-rc02", "3.0.0-rc03"))
                )
            }
            canRespond {
                responseCode(HttpStatusCode.BadRequest)
                responseType<Any>()
                description("请求参数错误")
            }
            canRespond {
                responseCode(HttpStatusCode.fromException(InvalidClientVersionException()))
                responseType<Any>()
                description("不合法的客户端版本号")
            }
        }
    }
}

private fun Route.incrementalDetailedDoc() {
    install(NotarizedRoute()) {
        get = GetInfo.builder {
            summary("获取可更新的版本详情")
            description("返回所有大于当前版本的更新版本的详细信息，包括版本号、下载地址、发布时间以及更新内容。")
            parameters(
                Parameter(
                    name = "clientVersion",
                    `in` = Parameter.Location.query,
                    required = true,
                    description = "客户端当前版本号。不合法的版本号会导致服务器返回461 Invalid Client Version错误。",
                    schema = TypeDefinition.STRING
                ),
                Parameter(
                    name = "clientPlatform",
                    `in` = Parameter.Location.query,
                    required = true,
                    description = "客户端平台，例：windows, android。不合法的值会导致服务器返回空的版本号列表。",
                    schema = TypeDefinition.STRING
                ),
                Parameter(
                    name = "clientArch",
                    `in` = Parameter.Location.query,
                    required = true,
                    description = "客户端架构，例：x86_64, arm64。不合法的值会导致服务器返回空的版本号列表。",
                    schema = TypeDefinition.STRING
                ),
                Parameter(
                    name = "releaseClass",
                    `in` = Parameter.Location.query,
                    required = true,
                    description = "更新版本的发布类型，可选值：alpha, beta, rc, stable。不合法的发布类型会导致服务器返回400 Bad Request错误。",
                    schema = TypeDefinition.STRING
                )
            )
            response {
                responseCode(HttpStatusCode.OK)
                responseType<ReleaseUpdatesResponse>()
                description("更新版本详细信息列表")
                examples(
                    "" to ReleaseUpdatesDetailedResponse(
                        listOf(
                            UpdateInfo(
                                "3.0.0-rc01",
                                listOf("https://d.myani.org/v3.0.0-rc01/ani-3.0.0-rc01.apk"),
                                1716604732,
                                """
                                    ## 主要更新
                                    - 重新设计资源选择器 #328
                                       - 了解每个数据源的查询结果, 失败时点击重试 #327 #309
                                       - 支持临时启用禁用数据源以应对未找到的情况
                                       - 区分 BT 源和在线源并增加提示 #330
                                    - 优化资源选择算法
                                      - 默认隐藏生肉资源, 可在设置中恢复显示
                                      - 支持番剧完结后隐藏单集 BT 资源, 默认启用, 可在设置关闭
                                      - 支持优先选择季度全集资源 #304
                                      - 自动优先选择本地缓存资源, 不再需要等待 #258 #260
                                    ## 次要更新
                                    - 提高弹幕匹配准确率 #338
                                    - 自动选择数据源时不再覆盖偏好设置
                                    - 自动选择数据源时不再保存不准确的字幕语言设置
                                    - 在切换数据源时, 将会按顺序自动取消筛选直到显示列表不为空
                                    - 在取消选择数据源的过滤时也记忆偏好设置
                                    - 修复有时候选择资源时会崩溃的问题
                                    - 优化数据源请求时的性能
                                    - 修复标题过长挤掉按钮的问题 #311
                                    - 修复会请求过多条目的问题
                                    - 修复条目缓存页可能有资源泄露的问题 #190
                                """.trimIndent()
                            ),
                        )
                    )
                )
            }
            canRespond {
                responseCode(HttpStatusCode.BadRequest)
                responseType<Any>()
                description("请求参数错误")
            }
            canRespond {
                responseCode(HttpStatusCode.fromException(InvalidClientVersionException()))
                responseType<Any>()
                description("不合法的客户端版本号")
            }
        }
    }
}
