/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package me.him188.ani.client.models


import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 *
 * @param bangumiToken
 * @param clientArch
 * @param clientOS
 * @param clientVersion
 */
@Serializable

data class AniMehim188anidanmakuprotocolBangumiLoginRequest(

    @SerialName(value = "bangumiToken") @Required val bangumiToken: kotlin.String,

    @SerialName(value = "clientArch") val clientArch: kotlin.String? = null,

    @SerialName(value = "clientOS") val clientOS: kotlin.String? = null,

    @SerialName(value = "clientVersion") val clientVersion: kotlin.String? = null

)

