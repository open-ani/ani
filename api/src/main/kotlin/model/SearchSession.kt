package me.him188.animationgarden.api.model

import kotlinx.coroutines.flow.Flow

interface SearchSession {
    val filter: SearchFilter

    val results: Flow<Topic>

    suspend fun nextPage(): List<Topic>?
}

data class SearchFilter(
    val keywords: String? = null,
    val category: TopicCategory? = null,
    val alliance: Alliance? = null,
    val ordering: SearchOrdering? = null
)

interface SearchOrdering {
    val id: String
    val name: String
}