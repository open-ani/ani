package me.him188.ani.datasources.bangumi.processing

import org.openapitools.client.models.Subject


fun Subject.nameCNOrName() =
    nameCn.takeIf { it.isNotBlank() } ?: name
