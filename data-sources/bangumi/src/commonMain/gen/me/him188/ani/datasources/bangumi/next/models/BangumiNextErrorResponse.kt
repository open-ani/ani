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

package me.him188.ani.datasources.bangumi.next.models


import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * default error response type
 *
 * @param code
 * @param error
 * @param message 
 * @param statusCode 
 */
@Serializable

data class BangumiNextErrorResponse(

    @SerialName(value = "code") @Required val code: kotlin.String,

    @SerialName(value = "error") @Required val error: kotlin.String,

    @SerialName(value = "message") @Required val message: kotlin.String,

    @SerialName(value = "statusCode") @Required val statusCode: kotlin.Int

)

