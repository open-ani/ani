package me.him188.ani.danmaku.protocol

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

@Serializable
data class BangumiLoginRequest(
    val bangumiToken: String,
    val clientVersion: String? = null,
    val clientPlatform: ClientPlatform? = null,
)

@Serializable
data class BangumiLoginResponse(
    val token: String,
)

@Serializable(ClientPlatform.Serializer::class)
sealed class ClientPlatform(val name: String) {
    data object Android : ClientPlatform("android")
    data object MacosAArch64 : ClientPlatform("macos_aarch64")
    data object MacosX8664 : ClientPlatform("macos_x86_64")
    data object WindowsX8664 : ClientPlatform("windows_x86_64")
    
    object Serializer : KSerializer<ClientPlatform> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ClientPlatform", PrimitiveKind.STRING)
        
        override fun deserialize(decoder: Decoder): ClientPlatform {
            return valueOf(decoder.decodeString())
        }
        
        override fun serialize(encoder: Encoder, value: ClientPlatform) {
            encoder.encodeString(value.name)
        }
    }

    override fun toString(): String {
        return name
    }
    
    companion object {
        fun values(): Array<ClientPlatform> {
            return arrayOf(Android, MacosAArch64, MacosX8664, WindowsX8664)
        }

        fun valueOf(value: String): ClientPlatform {
            return values().firstOrNull { it.name == value } ?: throw IllegalArgumentException("Invalid ClientPlatform $value")
        }
    }
}