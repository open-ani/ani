package me.him188.ani.danmaku.server

/**
 * Customizable environment variables for the server application.
 * By default, it reads the environment variables from the system.
 */
class EnvironmentVariables(
    /**
     * The port number on which the server listens to.
     */
    val port: Int? = System.getenv("PORT")?.toInt(),

    /**
     * Whether the server is running in testing mode.
     * If set to true, the server will use an in-memory database and avatar storage.
     * Otherwise, it will use the MongoDB database and AWS S3 for storage.
     */
    val testing: Boolean = System.getenv("TESTING") == "false",

    /**
     * The URL of the MongoDB database.
     * If [testing] is set to true, this value is not used.
     * Otherwise, the server will throw an exception if this value is not set.
     */
    val mongoDbConnectionString: String? = System.getenv("MONGODB_CONNECTION_STRING"),
)
