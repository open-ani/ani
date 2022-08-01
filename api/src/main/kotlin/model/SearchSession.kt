package me.him188.animationgarden.api.model

import kotlinx.coroutines.flow.Flow

public interface SearchSession {
    public val filter: SearchFilter

    public val results: Flow<Topic>
}

public class SearchFilter(
    public val keywords: String,
    public val category: TopicCategory,
    public val alliance: Alliance,
    public val ordering: SearchOrdering
)

public interface SearchOrdering {
    public val id: String
    public val name: String
}