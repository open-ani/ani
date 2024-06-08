package me.him188.ani.app.data.serializers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.danmaku.ui.DanmakuStyle

object DanmakuConfigSerializer : KSerializer<DanmakuConfig> {

    @Serializable
    private class DanmakuConfigData(
        val style: DanmakuStyleData = DanmakuStyleData(),
        val speed: Float = DanmakuConfig.Default.speed,
        val safeSeparation: Float = DanmakuConfig.Default.safeSeparation.value,
        val displayArea: Float = DanmakuConfig.Default.displayArea,
        val enableColor: Boolean = DanmakuConfig.Default.enableColor,
        val isDebug: Boolean = DanmakuConfig.Default.isDebug,
    )

    /**
     * @see DanmakuStyle
     */
    @Serializable
    private class DanmakuStyleData(
        val fontSize: Float = DanmakuStyle.Default.fontSize.value,
        val alpha: Float = DanmakuStyle.Default.alpha,
        val strokeColor: ULong = DanmakuStyle.Default.strokeColor.value,
        val strokeWidth: Float = DanmakuStyle.Default.strokeWidth,
    )

    override val descriptor: SerialDescriptor = DanmakuConfigData.serializer().descriptor

    override fun deserialize(decoder: Decoder): DanmakuConfig {
        val data = DanmakuConfigData.serializer().deserialize(decoder)

        return DanmakuConfig(
            style = DanmakuStyle(
                fontSize = data.style.fontSize.sp,
                alpha = data.style.alpha,
                strokeColor = Color(data.style.strokeColor),
                strokeWidth = data.style.strokeWidth,
            ),
            speed = data.speed,
            safeSeparation = data.safeSeparation.dp,
            displayArea = data.displayArea,
            enableColor = data.enableColor,
            isDebug = data.isDebug,
        )
    }

    override fun serialize(encoder: Encoder, value: DanmakuConfig) {
        val data = DanmakuConfigData(
            style = DanmakuStyleData(
                fontSize = value.style.fontSize.value,
                alpha = value.style.alpha,
                strokeColor = value.style.strokeColor.value,
                strokeWidth = value.style.strokeWidth,
            ),
            speed = value.speed,
            safeSeparation = value.safeSeparation.value,
            displayArea = value.displayArea,
            enableColor = value.enableColor,
            isDebug = value.isDebug,
        )

        return DanmakuConfigData.serializer().serialize(encoder, data)
    }
}