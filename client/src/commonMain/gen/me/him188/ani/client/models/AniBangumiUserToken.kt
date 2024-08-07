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
 * @param accessToken
 * @param expiresIn
 * @param refreshToken
 * @param userId 
 */
@Serializable

data class AniBangumiUserToken(

    @SerialName(value = "accessToken") @Required val accessToken: kotlin.String,

    @SerialName(value = "expiresIn") @Required val expiresIn: kotlin.Long,

    @SerialName(value = "refreshToken") @Required val refreshToken: kotlin.String,

    @SerialName(value = "userId") @Required val userId: kotlin.Int

)

