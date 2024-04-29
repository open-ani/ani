import java.util.Locale

// get current os
enum class Os {
    Windows,
    MacOS,
    Linux,
    Unknown
}

fun getOs(): Os {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    return when {
        os.contains("win") -> Os.Windows
        os.contains("mac") -> Os.MacOS
        os.contains("nux") -> Os.Linux
        else -> Os.Unknown
    }
}