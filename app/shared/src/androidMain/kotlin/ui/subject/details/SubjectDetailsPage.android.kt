package me.him188.ani.app.ui.subject.details

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.models.subject.CharacterType
import me.him188.ani.app.data.models.subject.Images
import me.him188.ani.app.data.models.subject.PersonCareer
import me.him188.ani.app.data.models.subject.PersonInfo
import me.him188.ani.app.data.models.subject.PersonType
import me.him188.ani.app.data.models.subject.RelatedCharacterInfo
import me.him188.ani.app.data.models.subject.RelatedPersonInfo
import me.him188.ani.app.data.models.subject.RelatedSubjectInfo
import me.him188.ani.app.data.models.subject.SubjectRelation
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.layout.rememberConnectedScrollState
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.subject.collection.TestSelfRatingInfo
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeButton
import me.him188.ani.app.ui.subject.details.components.CollectionData
import me.him188.ani.app.ui.subject.details.components.DetailsTab
import me.him188.ani.app.ui.subject.details.components.SelectEpisodeButtons
import me.him188.ani.app.ui.subject.details.components.SubjectCommentColumn
import me.him188.ani.app.ui.subject.details.components.SubjectDetailsDefaults
import me.him188.ani.app.ui.subject.details.components.TestSubjectInfo
import me.him188.ani.app.ui.subject.details.components.TestSubjectProgressInfos
import me.him188.ani.app.ui.subject.details.components.createTestAiringLabelState
import me.him188.ani.app.ui.subject.details.components.generateUiComment
import me.him188.ani.app.ui.subject.details.components.rememberTestCommentState
import me.him188.ani.app.ui.subject.details.components.rememberTestEditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.details.components.rememberTestSubjectProgressState
import me.him188.ani.app.ui.subject.rating.EditableRating
import me.him188.ani.app.ui.subject.rating.EditableRatingState
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.annotations.TestOnly
import kotlin.random.Random

internal fun testPersonInfo(
    name: String,
    type: PersonType = PersonType.Individual,
    careers: List<PersonCareer> = emptyList(),
    shortSummary: String = """一个测试人物""",
    locked: Boolean = false,
    images: Images? = null,
) = PersonInfo(
    id = Random.nextInt(),
    originalName = name,
    type = type,
    careers = careers,
    images = images,
    shortSummary = shortSummary,
    locked = locked,
)


internal fun testRelatedPersonInfo(
    name: String,
    relation: String,
    type: PersonType = PersonType.Individual,
    careers: List<PersonCareer> = emptyList(),
    shortSummary: String = """一个测试人物""",
    locked: Boolean = false,
    images: Images? = null,
) = RelatedPersonInfo(
    personInfo = testPersonInfo(name, type, careers, shortSummary, locked, images),
    relation = relation,
)

internal val TestSubjectStaffInfo = listOf(
    testRelatedPersonInfo("CloverWorks", relation = "动画制作", type = PersonType.Corporation),
    testRelatedPersonInfo("はまじあき", relation = "原作"),
    testRelatedPersonInfo("斎藤圭一郎", relation = "监督"),
    testRelatedPersonInfo("吉田恵里香", relation = "角色设计"),
    testRelatedPersonInfo("菊谷知樹", relation = "音乐"),
    testRelatedPersonInfo("けろりら", relation = "人物设计"),
)

internal fun testRelatedCharacterInfo(
    name: String,
    id: Int = 0,
    type: CharacterType = CharacterType.CHARACTER,
    relation: String = "主角",
    images: Images? = null,
    actors: List<PersonInfo> = emptyList(),
): RelatedCharacterInfo = RelatedCharacterInfo(id, name, type, relation, images, actors)

internal fun testRelatedCharacterInfo(
    name: String,
    actorName: String,
    id: Int = 0,
    type: CharacterType = CharacterType.CHARACTER,
    relation: String = "主角",
    images: Images? = null,
): RelatedCharacterInfo = RelatedCharacterInfo(
    id, name, type, relation, images,
    listOf(testPersonInfo(actorName, careers = listOf(PersonCareer.SEIYU))),
)

internal val TestSubjectCharacterList = listOf(
    testRelatedCharacterInfo("後藤ひとり", "青山吉能"),
    testRelatedCharacterInfo("伊地知虹夏", "鈴代紗弓"),
    testRelatedCharacterInfo("山田リョウ山田リョウ山田リョウ", "水野朔"),
    testRelatedCharacterInfo("喜多郁代", "長谷川育美"),
    testRelatedCharacterInfo("後藤直樹", "間島淳司"),
    testRelatedCharacterInfo("後藤美智代", "末柄里恵"),
)

internal fun testRelatedSubjectInfo(
    nameCn: String,
    relation: SubjectRelation?,
    id: Int = Random.nextInt(),
    name: String? = null,
    image: String? = null,
) = RelatedSubjectInfo(id, relation, name, nameCn, image)

internal val TestRelatedSubjects = listOf(
    testRelatedSubjectInfo("孤独摇滚 第二季", SubjectRelation.SEQUEL),
    testRelatedSubjectInfo("孤独摇滚 第零季", SubjectRelation.PREQUEL),
    testRelatedSubjectInfo("孤独摇滚 外传", SubjectRelation.DERIVED),
    testRelatedSubjectInfo("孤独摇滚 OAD", SubjectRelation.SPECIAL),
)

@Composable
fun rememberTestEditableRatingState(): EditableRatingState {
    val backgroundScope = rememberBackgroundScope()
    return remember {
        EditableRatingState(
            ratingInfo = mutableStateOf(TestSubjectInfo.ratingInfo),
            selfRatingInfo = mutableStateOf(TestSelfRatingInfo),
            enableEdit = mutableStateOf(true),
            isCollected = { true },
            onRate = { _ -> },
            backgroundScope.backgroundScope,
        )
    }
}

@OptIn(TestOnly::class)
@Preview
@Preview(device = Devices.TABLET)
@Composable
internal fun PreviewSubjectDetails() {
    ProvideCompositionLocalsForPreview {
        val state = remember {
            SubjectDetailsState(
                subjectInfoState = stateOf(TestSubjectInfo),
                selfCollectionTypeState = stateOf(UnifiedCollectionType.WISH),
                airingLabelState = createTestAiringLabelState(),
                charactersState = stateOf(TestSubjectCharacterList),
                personsState = stateOf(emptyList()),
                relatedSubjectsState = stateOf(TestRelatedSubjects),
            )
        }
        val connectedScrollState = rememberConnectedScrollState()
        SubjectDetailsPage(
            state = state,
            onClickOpenExternal = {},
            collectionData = {
                SubjectDetailsDefaults.CollectionData(
                    collectionStats = state.info.collection,
                )
            },
            collectionActions = {
                EditableSubjectCollectionTypeButton(
                    rememberTestEditableSubjectCollectionTypeState(),
                )
            },
            rating = {
                EditableRating(
                    state = rememberTestEditableRatingState(),
                )
            },
            selectEpisodeButton = {
                SubjectDetailsDefaults.SelectEpisodeButtons(
                    rememberTestSubjectProgressState(info = TestSubjectProgressInfos.ContinueWatching2),
                    onShowEpisodeList = {},
                )
            },
            connectedScrollState = connectedScrollState,
            detailsTab = {
                SubjectDetailsDefaults.DetailsTab(
                    info = TestSubjectInfo,
                    staff = TestSubjectStaffInfo,
                    characters = TestSubjectCharacterList,
                    relatedSubjects = TestRelatedSubjects,
                    Modifier.nestedScroll(connectedScrollState.nestedScrollConnection),
                )
            },
            commentsTab = {
                val lazyListState = rememberLazyListState()

                SubjectDetailsDefaults.SubjectCommentColumn(
                    state = rememberTestCommentState(commentList = generateUiComment(10)),
                    onClickUrl = { },
                    onClickImage = {},
                    connectedScrollState = connectedScrollState,
                    lazyListState = lazyListState,
                )
            },
            discussionsTab = {},
        )
    }
}