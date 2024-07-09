package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import me.him188.ani.app.data.model.subject.RelatedCharacterInfo
import me.him188.ani.app.data.model.subject.RelatedPersonInfo
import me.him188.ani.app.data.model.subject.SelfRatingInfo
import me.him188.ani.app.data.model.subject.SubjectAiringInfo
import me.him188.ani.app.data.model.subject.SubjectInfo
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import kotlin.coroutines.CoroutineContext

@Stable
class SubjectDetailsState(
    subjectInfo: Flow<SubjectInfo>,
    coverImageUrl: Flow<String>,
    selfRatingInfo: Flow<SelfRatingInfo>,
    selfCollectionType: Flow<UnifiedCollectionType>,
    airingInfo: Flow<SubjectAiringInfo>,
    persons: Flow<List<RelatedPersonInfo>>,
    characters: Flow<List<RelatedCharacterInfo>>,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    val info by subjectInfo.produceState<SubjectInfo>(SubjectInfo.Empty)

    private val coverImageUrlOrNull by coverImageUrl.produceState(null)
    val coverImageUrl by derivedStateOf { coverImageUrlOrNull ?: "" }

    private val selfRatingInfoOrNull by selfRatingInfo.produceState(null)
    val selfRatingInfo by derivedStateOf { selfRatingInfoOrNull ?: SelfRatingInfo.Empty }

    private val selfCollectionTypeOrNull by selfCollectionType.produceState(null)
    val selfCollectionType by derivedStateOf { selfCollectionTypeOrNull ?: UnifiedCollectionType.WISH }

    private val airingInfoOrNull by airingInfo.produceState(null)
    val airingInfo by derivedStateOf { airingInfoOrNull ?: SubjectAiringInfo.EmptyCompleted }

    val selfCollected by derivedStateOf { this.selfCollectionType != UnifiedCollectionType.NOT_COLLECTED }

    private val charactersOrNull by characters.produceState(null)
    val characters by derivedStateOf { charactersOrNull ?: emptyList() }

    private val personsOrNull by persons.onCompletion { if (it != null) emit(emptyList()) }.produceState(null)
    val persons by derivedStateOf { personsOrNull ?: emptyList() }

    /**
     * 有任何一个数据为空
     */
    val isLoading: Boolean by derivedStateOf {
        selfRatingInfoOrNull == null || selfCollectionTypeOrNull == null
                || charactersOrNull == null || info === SubjectInfo.Empty
                || personsOrNull == null || airingInfoOrNull == null
    }
}
