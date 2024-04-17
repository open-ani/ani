package me.him188.ani.danmaku.server.util.exception

abstract class HttpRequestException: Exception() {
    abstract val statusCode: Int
    abstract val statusMessage: String
}

class BadRequestException: HttpRequestException() {
    override val statusCode: Int = 400
    override val statusMessage: String = "Bad Request"
}

class UnauthorizedException: HttpRequestException() {
    override val statusCode: Int = 401
    override val statusMessage: String = "Unauthorized"
}

class NotFoundException: HttpRequestException() {
    override val statusCode: Int = 404
    override val statusMessage: String = "Not Found"
}

class EmptyDanmakuException: HttpRequestException() {
    override val statusCode: Int = 441
    override val statusMessage: String = "Empty Danmaku"
}

class InvalidDanmakuContentException: HttpRequestException() {
    override val statusCode: Int = 442
    override val statusMessage: String = "Invalid Danmaku Content"
}

class AcquiringTooMuchDanmakusException: HttpRequestException() {
    override val statusCode: Int = 451
    override val statusMessage: String = "Acquiring Too Much Danmakus"
}

class InternalServerErrorException: HttpRequestException() {
    override val statusCode: Int = 500
    override val statusMessage: String = "Internal Server Error"
}

class OperationFailedException: HttpRequestException() {
    override val statusCode: Int = 511
    override val statusMessage: String = "Operation Failed"
}