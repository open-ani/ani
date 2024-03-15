package me.him188.ani.app.data

/**
 * 数据层抛出的异常.
 */
sealed class RepositoryException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}

/**
 * 当网络错误等导致无法连接到数据源时抛出的异常.
 */
class RepositoryConnectionException : RepositoryException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
