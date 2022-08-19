package me.him188.animationgarden.desktop.app

import androidx.compose.runtime.*
import io.ktor.client.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Yaml
import java.io.File

@Immutable
@Serializable
data class AppSettings(
    /**
     * macOS 窗口沉浸, 把背景画入标题栏
     */
    @Stable
    val windowImmersed: Boolean = true,

    @Stable
    val proxy: ProxySettings = ProxySettings(),
)

@Immutable
@Serializable
data class ProxySettings(
    @Stable
    val mode: ProxyMode = ProxyMode.DISABLED,
    @Stable
    val http: HttpProxy = HttpProxy("http://localhost:7890"),
    @Stable
    val socks: SocksProxy = SocksProxy("localhost", 7890)
)

enum class ProxyMode {
    DISABLED,
    HTTP,
    SOCKS
}

@Stable
abstract class AppSettingsManager() {
    @Stable
    val value: MutableState<AppSettings> by lazy { mutableStateOf(loadImpl()) }

    inline fun mutate(block: AppSettings.() -> AppSettings) {
        value.value = value.value.let(block)
    }

    fun load() {
        value.value = loadImpl()
    }

    protected abstract fun loadImpl(): AppSettings

    fun save() {
        saveImpl(value.value)
    }

    protected abstract fun saveImpl(instance: AppSettings)

    @Composable
    fun attachAutoSave() {
        val instance by value
        LaunchedEffect(instance) {
            withContext(Dispatchers.IO) {
                save()
            }
        }
    }
}

@Stable
class LocalAppSettingsManagerImpl(
    private val file: File
) : AppSettingsManager() {
    override fun loadImpl(): AppSettings {
        if (!file.exists()) return AppSettings().also { saveImpl(it) }
        return Yaml.decodeFromString(AppSettings.serializer(), file.readText())
    }

    override fun saveImpl(instance: AppSettings) {
        file.parentFile.mkdir()
        file.writeText(Yaml.encodeToString(AppSettings.serializer(), instance))
    }
}

@Immutable
@Serializable
sealed class Proxy

@Serializable
@SerialName("http")
@Immutable
data class HttpProxy(
    @Stable
    val url: String
) : Proxy()

@Serializable
@SerialName("socks")
@Immutable
data class SocksProxy(
    @Stable
    val host: String,
    @Stable
    val port: Int,
) : Proxy()

@Stable
fun ProxySettings.toKtorProxy(): ProxyConfig? = when (mode) {
    ProxyMode.HTTP -> {
        ProxyBuilder.http(http.url)
    }
    ProxyMode.SOCKS -> {
        ProxyBuilder.socks(socks.host, socks.port)
    }
    ProxyMode.DISABLED -> null
}


@Stable
val LocalAppSettingsManager: ProvidableCompositionLocal<AppSettingsManager> = staticCompositionLocalOf {
    error("No AppSettingsManager provided in current context")
}

@Stable
object LocalAppSettings {
    @Stable
    val current
        @Composable
        get() = LocalAppSettingsManager.current.value.value
}