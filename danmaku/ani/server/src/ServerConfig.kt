package me.him188.ani.danmaku.server

import io.ktor.server.config.ApplicationConfig
import io.ktor.util.KtorDsl
import me.him188.ani.danmaku.server.util.CommandLineArgsParser
import me.him188.ani.danmaku.server.util.generateSecureRandomBytes
import java.io.File
import kotlin.time.Duration.Companion.days


/**
 * # 服务器配置类 [ServerConfig]
 *
 * 可通过以下几种方式加载配置，覆盖优先级从高到低：
 * - 调用[me.him188.ani.danmaku.server.ktor.getKtorServer]时传入[ServerConfig]参数或[ServerConfigBuilder] DSL参数
 * - 通过命令行参数传入配置项
 * - 在同目录的resources文件夹下放置HOCON配置文件application.conf
 * - 设置配置项对应的环境变量
 *
 * 部分配置项设有默认值，如果未加载任何值则会使用默认值。
 * 若有配置项未加载任何值且没有默认值，服务器启动时会抛出[IllegalStateException]异常。
 */
class ServerConfig(
    val port: Int,
    val host: String,
    val testing: Boolean,
    val rootDir: File,
    val mongoDbConnectionString: String?,
    val danmakuGetRequestMaxCountAllowed: Int,
    val jwt: JwtConfig,
    val githubAccessToken: String?,
) {
    class JwtConfig(
        val secret: ByteArray,
        val issuer: String,
        val audience: String,
        val expiration: Long,
        val realm: String,
    )
}

@KtorDsl
class ServerConfigBuilder private constructor(
    private val args: Array<String> = arrayOf(),
) {
    var port: Int? = null
    var host: String? = null
    var testing: Boolean? = null
    var rootDir: File? = null
    var mongoDbConnectionString: String? = null
    var danmakuGetRequestMaxCountAllowed: Int? = null
    private var jwt: JwtConfigBuilder = JwtConfigBuilder()
    var githubAccessToken: String? = null

    @KtorDsl
    class JwtConfigBuilder {
        var secret: ByteArray? = null
        var issuer: String? = null
        var audience: String? = null
        var expiration: Long? = null
        var realm: String? = null
    }

    fun jwt(configs: JwtConfigBuilder.() -> Unit) {
        jwt.apply(configs)
    }

    fun build(): ServerConfig {
        commandLineArgsPass()
        configFilePass()
        environmentVariablesPass()
        defaultValuePass()
        return ServerConfig(
            port = port ?: throw IllegalStateException("Port is not set"),
            host = host ?: throw IllegalStateException("Host is not set"),
            testing = testing ?: throw IllegalStateException("Config testing is not set"),
            rootDir = rootDir ?: throw IllegalStateException("Config rootDir is not set"),
            mongoDbConnectionString = mongoDbConnectionString,
            danmakuGetRequestMaxCountAllowed = danmakuGetRequestMaxCountAllowed
                ?: throw IllegalStateException("Config danmakuGetRequestMaxCountAllowed is not set"),
            jwt = ServerConfig.JwtConfig(
                secret = jwt.secret ?: throw IllegalStateException("JWT secret is not set"),
                issuer = jwt.issuer ?: throw IllegalStateException("JWT issuer is not set"),
                audience = jwt.audience ?: throw IllegalStateException("JWT audience is not set"),
                expiration = jwt.expiration ?: throw IllegalStateException("JWT expiration is not set"),
                realm = jwt.realm ?: throw IllegalStateException("JWT realm is not set"),
            ),
            githubAccessToken = githubAccessToken,
        )
    }

    private fun commandLineArgsPass() {
        val parser = CommandLineArgsParser(args)
        port = port ?: parser["port"]?.toIntOrNull()
        host = host ?: parser["host"]
        testing = testing ?: parser["testing"]?.toBoolean()
        rootDir = rootDir ?: parser["rootDir"]?.let { File(it) }
        mongoDbConnectionString = mongoDbConnectionString ?: parser["mongoDbConnectionString"]
        danmakuGetRequestMaxCountAllowed =
            danmakuGetRequestMaxCountAllowed ?: parser["danmakuGetRequestMaxCountAllowed"]?.toIntOrNull()
        jwt.secret = jwt.secret ?: parser["jwt.secret"]?.toByteArray()
        jwt.issuer = jwt.issuer ?: parser["jwt.issuer"]
        jwt.audience = jwt.audience ?: parser["jwt.audience"]
        jwt.expiration = jwt.expiration ?: parser["jwt.expiration"]?.toLongOrNull()
        jwt.realm = jwt.realm ?: parser["jwt.realm"]
        githubAccessToken = githubAccessToken ?: parser["githubAccessToken"]
    }

    private fun configFilePass() {
        val config = ApplicationConfig(null)
        port = port ?: config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull()
        host = host ?: config.propertyOrNull("ktor.deployment.host")?.getString()
        testing = testing ?: config.propertyOrNull("server.testing")?.getString()?.toBoolean()
        rootDir = rootDir ?: config.propertyOrNull("server.rootDir")?.getString()?.let { File(it) }
        danmakuGetRequestMaxCountAllowed =
            danmakuGetRequestMaxCountAllowed ?: config.propertyOrNull("server.danmakuGetRequestMaxCountAllowed")
                ?.getString()?.toIntOrNull()
        jwt.issuer = jwt.issuer ?: config.propertyOrNull("jwt.issuer")?.getString()
        jwt.audience = jwt.audience ?: config.propertyOrNull("jwt.audience")?.getString()
        jwt.expiration = jwt.expiration ?: config.propertyOrNull("jwt.expiration")?.getString()?.toLongOrNull()
        jwt.realm = jwt.realm ?: config.propertyOrNull("jwt.realm")?.getString()
        githubAccessToken = githubAccessToken ?: config.propertyOrNull("github.accessToken")?.getString()
    }

    private fun environmentVariablesPass() {
        port = port ?: System.getenv("KTOR_PORT")?.toIntOrNull()
        host = host ?: System.getenv("KTOR_HOST")
        testing = testing ?: (System.getenv("SERVER_TESTING")?.toBoolean())
        rootDir = rootDir ?: System.getenv("SERVER_ROOT_DIR")?.let { File(it) }
        mongoDbConnectionString = mongoDbConnectionString ?: System.getenv("MONGODB_CONNECTION_STRING")
        danmakuGetRequestMaxCountAllowed =
            danmakuGetRequestMaxCountAllowed ?: System.getenv("DANMAKU_GET_REQUEST_MAX_COUNT_ALLOWED")?.toIntOrNull()
        jwt.secret = jwt.secret ?: System.getenv("JWT_SECRET")?.toByteArray()
        jwt.issuer = jwt.issuer ?: System.getenv("JWT_ISSUER")
        jwt.audience = jwt.audience ?: System.getenv("JWT_AUDIENCE")
        jwt.expiration = jwt.expiration ?: System.getenv("JWT_EXPIRATION")?.toLongOrNull()
        jwt.realm = jwt.realm ?: System.getenv("JWT_REALM")
        githubAccessToken = githubAccessToken ?: System.getenv("GITHUB_ACCESS_TOKEN")
    }

    private fun defaultValuePass() {
        port = port ?: 4394
        host = host ?: "0.0.0.0"
        testing = testing ?: false
        rootDir = rootDir ?: File(".")
        danmakuGetRequestMaxCountAllowed = danmakuGetRequestMaxCountAllowed ?: 8000
        jwt.secret = jwt.secret ?: generateSecureRandomBytes()
        jwt.expiration = jwt.expiration ?: 7.days.inWholeMilliseconds
        jwt.realm = jwt.realm ?: "Ani Danmaku"
    }

    companion object {
        fun create(args: Array<String> = arrayOf(), configs: ServerConfigBuilder.() -> Unit) =
            ServerConfigBuilder(args).apply(configs)
    }
}