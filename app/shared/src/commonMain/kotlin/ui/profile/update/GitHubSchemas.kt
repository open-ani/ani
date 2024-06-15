package me.him188.ani.app.ui.profile.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Release(
    @SerialName("url") val url: String = "",
    @SerialName("assets_url") val assetsUrl: String = "",
    @SerialName("upload_url") val uploadUrl: String = "",
    @SerialName("html_url") val htmlUrl: String = "",
    @SerialName("id") val id: Int = 0,
    @SerialName("author") val author: User = User(),
    @SerialName("node_id") val nodeId: String = "",
    @SerialName("tag_name") val tagName: String = "",
    @SerialName("target_commitish") val targetCommitish: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("draft") val draft: Boolean = false,
    @SerialName("prerelease") val prerelease: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("published_at") val publishedAt: String = "",
    @SerialName("assets") val assets: List<Asset> = emptyList(),
    @SerialName("tarball_url") val tarballUrl: String = "",
    @SerialName("zipball_url") val zipballUrl: String = "",
    @SerialName("body") val body: String = "",
    @SerialName("discussion_url") val discussionUrl: String = "",
    @SerialName("reactions") val reactions: Reactions = Reactions()
)

@Serializable
data class User(
    @SerialName("login") val login: String = "",
    @SerialName("id") val id: Int = 0,
    @SerialName("node_id") val nodeId: String = "",
    @SerialName("avatar_url") val avatarUrl: String = "",
    @SerialName("gravatar_id") val gravatarId: String = "",
    @SerialName("url") val url: String = "",
    @SerialName("html_url") val htmlUrl: String = "",
    @SerialName("followers_url") val followersUrl: String = "",
    @SerialName("following_url") val followingUrl: String = "",
    @SerialName("gists_url") val gistsUrl: String = "",
    @SerialName("starred_url") val starredUrl: String = "",
    @SerialName("subscriptions_url") val subscriptionsUrl: String = "",
    @SerialName("organizations_url") val organizationsUrl: String = "",
    @SerialName("repos_url") val reposUrl: String = "",
    @SerialName("events_url") val eventsUrl: String = "",
    @SerialName("received_events_url") val receivedEventsUrl: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("site_admin") val siteAdmin: Boolean = false
)

@Serializable
data class Asset(
    @SerialName("url") val url: String = "",
    @SerialName("id") val id: Int = 0,
    @SerialName("node_id") val nodeId: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("label") val label: String? = null,
    @SerialName("uploader") val uploader: User = User(),
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("state") val state: String = "",
    @SerialName("size") val size: Int = 0,
    @SerialName("download_count") val downloadCount: Int = 0,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("browser_download_url") val browserDownloadUrl: String = ""
)

@Serializable
data class Reactions(
    @SerialName("url") val url: String = "",
    @SerialName("total_count") val totalCount: Int = 0,
    @SerialName("+1") val plusOne: Int = 0,
    @SerialName("-1") val minusOne: Int = 0,
    @SerialName("laugh") val laugh: Int = 0,
    @SerialName("hooray") val hooray: Int = 0,
    @SerialName("confused") val confused: Int = 0,
    @SerialName("heart") val heart: Int = 0,
    @SerialName("rocket") val rocket: Int = 0,
    @SerialName("eyes") val eyes: Int = 0
)
