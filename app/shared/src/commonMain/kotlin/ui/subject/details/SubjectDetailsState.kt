package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.subject.RelatedCharacterInfo
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
    characters: Flow<List<RelatedCharacterInfo>>,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    private val _info = subjectInfo.produceState(SubjectInfo.Empty)
    val info by _info

    private val _selfCollectionType = selfCollectionType.produceState(UnifiedCollectionType.NOT_COLLECTED)
    val selfCollectionType by _selfCollectionType
    private val _produceState = characters.produceState(emptyList())
    val produceState by _produceState

    val isLoading: Boolean by derivedStateOf {
        _info.isLoading || _selfCollectionType.isLoading || _produceState.isLoading
    }
}
