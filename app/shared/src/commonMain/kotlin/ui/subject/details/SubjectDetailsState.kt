package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.models.subject.RelatedCharacterInfo
import me.him188.ani.app.data.models.subject.RelatedPersonInfo
import me.him188.ani.app.data.models.subject.RelatedSubjectInfo
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.subject.collection.components.AiringLabelState
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import kotlin.coroutines.CoroutineContext

@Stable
class SubjectDetailsState(
    // TODO: maybe refactor 
    subjectInfo: Flow<SubjectInfo>,
    coverImageUrl: Flow<String>,
    selfCollectionType: Flow<UnifiedCollectionType>,
    persons: Flow<List<RelatedPersonInfo>>,
    characters: Flow<List<RelatedCharacterInfo>>,
    relatedSubjects: Flow<List<RelatedSubjectInfo>>,
    val airingLabelState: AiringLabelState,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    val info by subjectInfo.produceState<SubjectInfo>(SubjectInfo.Empty)

    private val coverImageUrlOrNull by coverImageUrl.produceState(null)
    val coverImageUrl by derivedStateOf { coverImageUrlOrNull ?: "" }

    private val selfCollectionTypeOrNull by selfCollectionType.produceState(null)
    val selfCollectionType by derivedStateOf { selfCollectionTypeOrNull ?: UnifiedCollectionType.WISH }

    val selfCollected by derivedStateOf { this.selfCollectionType != UnifiedCollectionType.NOT_COLLECTED }

    private val charactersOrNull by characters.produceState(null)
    val characters by derivedStateOf { charactersOrNull ?: emptyList() }

    private val personsOrNull by persons.produceState(null)
    val persons by derivedStateOf { personsOrNull ?: emptyList() }

    private val relatedSubjectsOrNull by relatedSubjects.produceState(null)
    val relatedSubjects by derivedStateOf { relatedSubjectsOrNull ?: emptyList() }

    /**
     * 有任何一个数据为空
     */
    val isLoading: Boolean by derivedStateOf {
        selfCollectionTypeOrNull == null
                || charactersOrNull == null || info === SubjectInfo.Empty
                || personsOrNull == null
                || relatedSubjectsOrNull == null
                || airingLabelState.isLoading
    }
}
