package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.subject.RelatedCharacterInfo
import me.him188.ani.app.data.subject.RelatedPersonInfo
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import kotlin.coroutines.CoroutineContext

@Stable
class SubjectDetailsState(
    subjectInfo: Flow<SubjectInfo>,
    val coverImageUrl: String?,
    selfCollectionType: Flow<UnifiedCollectionType>,
    persons: Flow<List<RelatedPersonInfo>>,
    characters: Flow<List<RelatedCharacterInfo>>,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    val info by subjectInfo.produceState<SubjectInfo>(SubjectInfo.Empty)

    private val selfCollectionTypeOrNull by selfCollectionType.produceState(null)
    val selfCollectionType by derivedStateOf { selfCollectionTypeOrNull ?: UnifiedCollectionType.WISH }

    private val charactersOrNull by characters.produceState(null)
    val characters by derivedStateOf { charactersOrNull ?: emptyList() }

    private val personsOrNull by persons.produceState(null)
    private val persons by derivedStateOf { personsOrNull ?: emptyList() }

    val staff by derivedStateOf {
        RelatedPersonInfo.sortList(this.persons)
    }

    /**
     * 有任何一个数据为空
     */
    val isLoading: Boolean by derivedStateOf {
        // We must split them and ensure they are called. 
        // Otherwise, the derivedStateOf will not be called when the value is changed.
        val selfCollectionTypeLoading = selfCollectionTypeOrNull == null
        val charactersLoading = charactersOrNull == null
        val infoLoading = info === SubjectInfo.Empty
        val personsLoading = personsOrNull == null
        infoLoading || selfCollectionTypeLoading || charactersLoading || personsLoading
    }
}
