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
 * 
 *
 * @param avatar
 * @param id 
 * @param nickname 
 */
@Serializable

data class BangumiNextGetSubjectEpisodeComments200ResponseInnerAllOfUser(

    @SerialName(value = "avatar") @Required val avatar: BangumiNextGetSubjectEpisodeComments200ResponseInnerAllOfUserAvatar,

    @SerialName(value = "id") @Required val id: kotlin.Int,

    @SerialName(value = "nickname") @Required val nickname: kotlin.String

)
