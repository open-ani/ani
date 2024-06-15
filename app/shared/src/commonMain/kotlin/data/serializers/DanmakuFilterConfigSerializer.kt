package me.him188.ani.app.data.serializers


import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.him188.ani.danmaku.ui.DanmakuRegexFilterConfig
import me.him188.ani.danmaku.ui.DanmakuRegexFilter


object DanmakuFilterConfigSerializer: KSerializer<DanmakuRegexFilterConfig> {

    @Serializable
    private class DanmakuFilterConfigData(
        val danmakuFilterOn: Boolean = DanmakuRegexFilterConfig.Default.danmakuRegexFilterOn,
        val danmakuFilterList: List<DanmakuRegexFilter> = DanmakuRegexFilterConfig.Default.danmakuRegexFilterList,
    )

    override val descriptor: SerialDescriptor = DanmakuFilterConfigData.serializer().descriptor

    override fun deserialize(decoder: Decoder): DanmakuRegexFilterConfig {
        val value = DanmakuFilterConfigData.serializer().deserialize(decoder)

        return DanmakuRegexFilterConfig(
            danmakuRegexFilterOn = value.danmakuFilterOn,
            danmakuRegexFilterList = value.danmakuFilterList,
        )
    }

    override fun serialize(encoder: Encoder, value: DanmakuRegexFilterConfig) {
        val data = DanmakuFilterConfigData(
            danmakuFilterOn = value.danmakuRegexFilterOn,
            danmakuFilterList = value.danmakuRegexFilterList,
        )

        return DanmakuFilterConfigData.serializer().serialize(encoder, data)
    }
}


object DanmakuReFilterSerializer: KSerializer<DanmakuRegexFilter> {
    @Serializable
    private class DanmakuReFilterData(
        val re: String = DanmakuRegexFilter.Default.re,
        val isEnabled: Boolean = DanmakuRegexFilter.Default.isEnabled,
        val name: String = DanmakuRegexFilter.Default.name,
        val instanceID: String = DanmakuRegexFilter.Default.instanceID,
    )

    override val descriptor: SerialDescriptor = DanmakuReFilterData.serializer().descriptor

    override fun deserialize(decoder: Decoder): DanmakuRegexFilter {
        val value = DanmakuReFilterData.serializer().deserialize(decoder)

        return DanmakuRegexFilter(
            re = value.re,
            isEnabled = value.isEnabled,
            name = value.name,
            instanceID = value.instanceID,
        )
    }

    override fun serialize(encoder: Encoder, value: DanmakuRegexFilter) {
        val data = DanmakuReFilterData(
            re = value.re,
            isEnabled = value.isEnabled,
            name = value.name,
            instanceID = value.instanceID,
        )

        return DanmakuReFilterData.serializer().serialize(encoder, data)
    }
}