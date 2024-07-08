package me.him188.ani.datasources.bangumi.next.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.bangumi.models.EnumValueSerializer

/**
 * - `Mod`: 不知道 - `Normal`: 不知道 - `All`: 不知道
 *
 * Values: Mod,Normal,All
 */
@Serializable(BangumiNextListGroupMembersByNameTypeParameterAsString::class)
enum class BangumiNextListGroupMembersByNameTypeParameter(val value: Int) { // TODO: field name

    @SerialName(value = "mod")
    Mod(0),

    @SerialName(value = "normal")
    Normal(1),

    @SerialName(value = "all")
    All(2);

    /**
     * Override [toString()] to avoid using the enum variable name as the value, and instead use
     * the actual value defined in the API spec file.
     *
     * This solves a problem when the variable name and its value are different, and ensures that
     * the client sends the correct enum values to the server always.
     */
    override fun toString(): String = value.toString()
}

private object BangumiNextListGroupMembersByNameTypeParameterAsString :
    EnumValueSerializer<BangumiNextListGroupMembersByNameTypeParameter>(
        "BangumiNextListGroupMembersByNameTypeParameter",
        BangumiNextListGroupMembersByNameTypeParameter.entries,
        { it.value },
    )