package me.him188.ani.datasources.bangumi.next.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.bangumi.models.EnumValueSerializer

/**
 * - `1`: 不知道 - `2`: 不知道 - `3`: 不知道 - `4`: 不知道 - `6`: 不知道
 */
@Serializable(BangumiNextSubjectTypeAsInt::class)
enum class BangumiNextSubjectType(val value: Int) { // TODO: field name

    @SerialName(value = "1")
    One(1),

    @SerialName(value = "2")
    Two(2),

    @SerialName(value = "3")
    Three(3),

    @SerialName(value = "4")
    Four(4),

    @SerialName(value = "6")
    Six(6);

    /**
     * Override [toString()] to avoid using the enum variable name as the value, and instead use
     * the actual value defined in the API spec file.
     *
     * This solves a problem when the variable name and its value are different, and ensures that
     * the client sends the correct enum values to the server always.
     */
    override fun toString(): String = value.toString()

}

private object BangumiNextSubjectTypeAsInt : EnumValueSerializer<BangumiNextSubjectType>(
    "BangumiNextSubjectType", BangumiNextSubjectType.entries, { it.value },
)