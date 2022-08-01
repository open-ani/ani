package me.him188.animationgarden.api.impl.model

import me.him188.animationgarden.api.model.Alliance
import me.him188.animationgarden.api.model.TopicCategory

internal class TopicCategoryImpl(
    override val id: String,
    override val name: String
) : TopicCategory

internal class AllianceImpl(
    override val id: String,
    override val name: String
) : Alliance