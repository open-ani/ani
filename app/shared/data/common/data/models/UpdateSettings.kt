package me.him188.ani.app.data.models

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.danmaku.protocol.ReleaseClass

@Immutable
@Serializable
data class UpdateSettings(
    val autoCheckUpdate: Boolean = true,
    val releaseClass: ReleaseClass = ReleaseClass.STABLE,
    @Suppress("PropertyName") @Transient val _placeholder: Int = 0,
) {
    // 默认的更新设置投票:
    // https://uk.surveymonkey.com/analyze/J384Ec5GDMJDT1a9quO1xHsDQuCCh1R4qNPOY1VPegE_3D?tab_clicked=1
    // 总结:
    // - 检查更新时选择 alpha 和 stable 的人数相同
    // - 自动更新时大家更希望更新到 stable

    companion object {
        @Stable
        val Default = UpdateSettings()
    }
}