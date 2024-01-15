package me.him188.ani.datasources.bangumi.processing

fun Int.fixToString(length: Int, prefix: Char = '0'): String {
    val str = this.toString()
    return if (str.length >= length) {
        str
    } else {
        prefix.toString().repeat(length - str.length) + str
    }
}

fun Long.fixToString(length: Int, prefix: Char = '0'): String {
    val str = this.toString()
    return if (str.length >= length) {
        str
    } else {
        prefix.toString().repeat(length - str.length) + str
    }
}

fun Float.fixToString(length: Int, prefix: Char = '0'): String {
    val str = this.toString()
    return if (str.length >= length) {
        str
    } else {
        prefix.toString().repeat(length - str.length) + str
    }
}

fun String.fixToString(length: Int, prefix: Char): String {
    val str = this.toString()
    return if (str.length >= length) {
        str
    } else {
        prefix.toString().repeat(length - str.length) + str
    }
}
