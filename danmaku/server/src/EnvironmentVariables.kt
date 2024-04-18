package me.him188.ani.danmaku.server

/**
 * Customizable environment variables for the server application.
 * By default, it reads the environment variables from the system.
 */
class EnvironmentVariables(
    /**
     * The port number on which the server listens to.
     * If not set, the server will use the default port 4394.
     */
    val port: Int? = System.getenv("PORT")?.toInt(),

    /**
     * Whether the server is running in testing mode.
     * If set to true, the server will use an in-memory database and avatar storage.
     * Otherwise, it will use the MongoDB database for storage.
     */
    val testing: Boolean = System.getenv("TESTING") == "true",

    /**
     * The URL of the MongoDB database.
     * If [testing] is set to true, this value is not used.
     * Otherwise, the server will throw an [IllegalStateException] if this value is not set.
     */
    val mongoDbConnectionString: String? = System.getenv("MONGODB_CONNECTION_STRING"),
    
    /**
     * The 32-byte secret key used to sign the JWT token.
     * If not set, the server will generate a random secret key.
     */
    val jwtSecret: String? = System.getenv("JWT_SECRET"),
    
    /**
     * The issuer of the JWT token.
     * If not set, the server will throw an [IllegalStateException].
     */
    val jwtIssuer: String? = System.getenv("JWT_ISSUER"),
    
    /**
     * The audience of the JWT token.
     * If not set, the server will throw an [IllegalStateException].
     */
    val jwtAudience: String? = System.getenv("JWT_AUDIENCE"),
    
    /**
     * The expiration time of the JWT token in milliseconds.
     * If not set, the server will use the default expiration time of 7 days.
     */
    val jwtExpiration: Long? = System.getenv("JWT_EXPIRATION")?.toLong(),
    
    /**
     * The realm of the JWT token.
     * If not set, the server will use the default realm name "Ani Danmaku".
     */
    val jwtRealm: String? = System.getenv("JWT_REALM"),
)
