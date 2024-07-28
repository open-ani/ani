package me.him188.ani.test

actual fun Any.readTestResourceAsString(path: String): String {
    return this::class.java
        .getResource(path)!!
        .readText()
}