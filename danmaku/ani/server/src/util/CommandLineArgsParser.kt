package me.him188.ani.danmaku.server.util

class CommandLineArgsParser(
    args: Array<String>,
) {
    private val argsMap: Map<String, String> = args.mapNotNull {
        val split = it.split("=")
        if (split.size != 2 || split[0].isBlank() || split[1].isBlank()) return@mapNotNull null
        if (split[0][0] != '-') return@mapNotNull null
        val key = split[0].substring(1).trim()
        if (key.isEmpty()) return@mapNotNull null
        val value = split[1].trim()
        key to value
    }.toMap()

    operator fun get(key: String): String? {
        return argsMap[key]
    }
}