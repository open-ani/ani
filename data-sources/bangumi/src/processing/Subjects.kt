package me.him188.ani.datasources.bangumi.processing

import org.openapitools.client.models.SlimSubject
import org.openapitools.client.models.Subject


fun Subject.nameCNOrName() =
    nameCn.takeIf { it.isNotBlank() } ?: name

fun SlimSubject.nameCNOrName() =
    nameCn.takeIf { it.isNotBlank() } ?: name

