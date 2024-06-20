package me.him188.ani.app.data.serializers


import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.him188.ani.danmaku.ui.DanmakuFilterConfig


object DanmakuFilterConfigSerializer: KSerializer<DanmakuFilterConfig> {

    @Serializable
    private class DanmakuFilterConfigData(
        val danmakuFilterOn: Boolean = DanmakuFilterConfig.Default.danmakuFilterOn,
        val danmakuFilterList: List<String> = DanmakuFilterConfig.Default.danmakuFilterList,
    )

    override val descriptor: SerialDescriptor = DanmakuFilterConfigData.serializer().descriptor

    override fun deserialize(decoder: Decoder): DanmakuFilterConfig {
        val value = DanmakuFilterConfigData.serializer().deserialize(decoder)

        return DanmakuFilterConfig(
            danmakuFilterOn = value.danmakuFilterOn,
            danmakuFilterList = value.danmakuFilterList,
        )
    }

    override fun serialize(encoder: Encoder, value: DanmakuFilterConfig) {
        val data = DanmakuFilterConfigData(
            danmakuFilterOn = value.danmakuFilterOn,
            danmakuFilterList = value.danmakuFilterList,
        )

        return DanmakuFilterConfigData.serializer().serialize(encoder, data)
    }
}