package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.data.models.subject.CharacterType
import me.him188.ani.app.data.models.subject.Images
import me.him188.ani.app.data.models.subject.PersonCareer
import me.him188.ani.app.data.models.subject.PersonInfo
import me.him188.ani.app.data.models.subject.PersonType
import me.him188.ani.app.data.models.subject.RelatedCharacterInfo
import me.him188.ani.app.data.models.subject.RelatedPersonInfo
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.layout.rememberConnectedScrollState
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.app.ui.subject.collection.EditableSubjectCollectionTypeButton
import me.him188.ani.app.ui.subject.collection.TestSelfRatingInfo
import me.him188.ani.app.ui.subject.details.components.CollectionData
import me.him188.ani.app.ui.subject.details.components.DetailsTab
import me.him188.ani.app.ui.subject.details.components.SelectEpisodeButton
import me.him188.ani.app.ui.subject.details.components.SubjectDetailsDefaults
import me.him188.ani.app.ui.subject.details.components.TestSubjectAiringInfo
import me.him188.ani.app.ui.subject.details.components.TestSubjectInfo
import me.him188.ani.app.ui.subject.details.components.rememberTestEditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.rating.EditableRating
import me.him188.ani.app.ui.subject.rating.EditableRatingState
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
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

@Preview
@Preview(device = Devices.TABLET)
@Composable
internal fun PreviewSubjectDetails() {
    ProvideCompositionLocalsForPreview {
        val vm = remember {
            SubjectDetailsViewModel(400602)
        }
        val state = remember {
            SubjectDetailsState(
                subjectInfo = MutableStateFlow(TestSubjectInfo),
                coverImageUrl = MutableStateFlow("https://ui-avatars.com/api/?name=John+Doe"),
                selfCollectionType = MutableStateFlow(UnifiedCollectionType.WISH),
                airingInfo = MutableStateFlow(TestSubjectAiringInfo),
                characters = MutableStateFlow(TestSubjectCharacterList),
                persons = MutableStateFlow(emptyList()),
                parentCoroutineContext = vm.backgroundScope.coroutineContext,
            )
        }
        val connectedScrollState = rememberConnectedScrollState()
        SubjectDetailsPage(
            state = state,
            onClickOpenExternal = {},
            collectionData = {
                SubjectDetailsDefaults.CollectionData(
                    collectionStats = vm.subjectDetailsState.info.collection,
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
                SubjectDetailsDefaults.SelectEpisodeButton({})
            },
            connectedScrollState = connectedScrollState,
            detailsTab = {
                SubjectDetailsDefaults.DetailsTab(
                    info = TestSubjectInfo,
                    staff = TestSubjectStaffInfo,
                    characters = TestSubjectCharacterList,
                    Modifier.nestedScroll(connectedScrollState.nestedScrollConnection),
                )
            },
            commentsTab = {},
            discussionsTab = {},
        )
    }
}