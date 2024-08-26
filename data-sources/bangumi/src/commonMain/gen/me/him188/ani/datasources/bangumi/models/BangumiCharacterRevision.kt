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

package me.him188.ani.datasources.bangumi.models

import me.him188.ani.datasources.bangumi.models.BangumiCharacterRevisionDataItem
import me.him188.ani.datasources.bangumi.models.BangumiCreator

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * 
 *
 * @param id
 * @param type
 * @param summary
 * @param createdAt
 * @param creator
 * @param `data` 
 */
@Serializable

data class BangumiCharacterRevision(

    @SerialName(value = "id") @Required val id: kotlin.Int,

    @SerialName(value = "type") @Required val type: kotlin.Int,

    @SerialName(value = "summary") @Required val summary: kotlin.String,

    @SerialName(value = "created_at") @Required val createdAt: kotlinx.datetime.Instant,

    @SerialName(value = "creator") val creator: BangumiCreator? = null,

    @SerialName(value = "data") val `data`: kotlin.collections.Map<kotlin.String, BangumiCharacterRevisionDataItem>? = null

)

