package me.him188.animationgarden.api.model

import kotlinx.coroutines.flow.Flow

public interface SearchSession {
    public val filter: SearchFilter

    public val results: Flow<Topic>

    public suspend fun nextPage(): List<Topic>?
}

public data class SearchFilter(
    public val keywords: String? = null,
    public val category: TopicCategory? = null,
    public val alliance: Alliance? = null,
    public val ordering: SearchOrdering? = null
)

public interface SearchOrdering {
    public val id: String
    public val name: String
}