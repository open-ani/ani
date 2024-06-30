package me.him188.ani.app.ui.subject.details.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.subject.SubjectAiringInfo
import me.him188.ani.app.data.subject.SubjectAiringKind
import me.him188.ani.app.data.subject.SubjectCollectionStats
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.data.subject.Tag
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.rating.TestRatingInfo
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

internal val TestCollectionStats = SubjectCollectionStats(
    wish = 100,
    doing = 200,
    done = 300,
    onHold = 400,
    dropped = 500,
)

internal val TestSubjectInfo = SubjectInfo.Empty.copy(
    nameCn = "孤独摇滚！",
    name = "ぼっち・ざ・ろっく！",
    date = "2023-10-01",
    summary = """
        作为网络吉他手“吉他英雄”而广受好评的后藤一里，在现实中却是个什么都不会的沟通障碍者。一里有着组建乐队的梦想，但因为不敢向人主动搭话而一直没有成功，直到一天在公园中被伊地知虹夏发现并邀请进入缺少吉他手的“结束乐队”。可是，完全没有和他人合作经历的一里，在人前完全发挥不出原本的实力。为了努力克服沟通障碍，一里与“结束乐队”的成员们一同开始努力……
    """.trimIndent(),
    tags = listOf(
        Tag("芳文社", 7098),
        Tag("音乐", 5000),
        Tag("CloverWorks", 5000),
        Tag("轻百合", 4000),
        Tag("日常", 3758),
    ),
    ratingInfo = TestRatingInfo,
    collection = TestCollectionStats,
)

internal const val TestCoverImage = "https://ui-avatars.com/api/?name=John+Doe"

internal val TestSubjectAiringInfo = SubjectAiringInfo.EmptyCompleted

@Composable
@Preview
@Preview(device = Devices.TABLET)
fun PreviewSubjectDetailsHeaderCompleted() {
    PreviewSubjectDetailsHeader(
        airingInfo = TestSubjectAiringInfo,
        subjectInfo = TestSubjectInfo,
    )
}

@Composable
@Preview
fun PreviewSubjectDetailsHeaderCompletedLong() {
    PreviewSubjectDetailsHeader(
        airingInfo = TestSubjectAiringInfo,
        subjectInfo = TestSubjectInfo.copy(
            nameCn = "孤独摇滚".repeat(20),
        ),
    )
}

@Composable
@Preview
@Preview(device = Devices.TABLET)
fun PreviewSubjectDetailsHeaderOnAir() {
    PreviewSubjectDetailsHeader(
        airingInfo = TestSubjectAiringInfo.copy(
            kind = SubjectAiringKind.ON_AIR,
            episodeCount = 24,
            latestSort = EpisodeSort(20),
        ),
    )
}

@Composable
fun PreviewSubjectDetailsHeader(
    airingInfo: SubjectAiringInfo,
    subjectInfo: SubjectInfo = TestSubjectInfo,
) {
    ProvideCompositionLocalsForPreview {
        SubjectDetailsHeader(
            subjectInfo,
            TestCoverImage,
            selfRatingScore = 7,
            airingInfo = airingInfo,
            onClickRating = {},
            collectionData = {
                SubjectDetailsDefaults.CollectionData(
                    collectionStats = subjectInfo.collection,
                )
            },
            collectionAction = {
                SubjectDetailsDefaults.CollectionAction(
                    UnifiedCollectionType.WISH,
                    onSetCollectionType = { },
                )
            },
            selectEpisodeButton = {
                SubjectDetailsDefaults.SelectEpisodeButton({})
            },
        )
    }
}


//@Composable
//@Preview(device = Devices.TABLET)
//private fun PreviewHeaderScaffoldWide() {
//    ProvideCompositionLocalsForPreview {
//        val info = TestSubjectInfo
//        SubjectDetailsHeaderWide(
//            coverImageUrl = null,
//            ratingInfo = info.ratingInfo,
//            selfRatingScore = 7,
//            onClickRating = {},
//            title = { Text(text = info.displayName) },
//            subtitle = { Text(text = info.name) },
//            seasonTags = {
//                OutlinedTag {
//                    Text(renderSubjectSeason(info.publishDate))
//                }
//            },
//            collectionData = {
//                SubjectDetailsDefaults.CollectionData(info)
//            },
//            collectionAction = {
//                SubjectDetailsDefaults.CollectionAction(
//                    UnifiedCollectionType.WISH,
//                    onSetCollectionType = { },
//                )
//            },
//            selectEpisodeButton = {
//                SubjectDetailsDefaults.SelectEpisodeButton({})
//            },
//        )
//    }
//}
//
