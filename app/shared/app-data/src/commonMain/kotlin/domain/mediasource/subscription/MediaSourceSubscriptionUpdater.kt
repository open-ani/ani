/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.mediasource.subscription

import kotlinx.coroutines.flow.first
import me.him188.ani.app.data.models.ApiFailure
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.valueOrElse
import me.him188.ani.app.data.repository.MediaSourceSubscriptionRepository
import me.him188.ani.app.domain.media.fetch.MediaSourceManager
import me.him188.ani.app.domain.media.fetch.updateMediaSourceArguments
import me.him188.ani.app.domain.mediasource.instance.MediaSourceSave
import me.him188.ani.app.domain.mediasource.codec.ExportedMediaSourceData
import me.him188.ani.app.domain.mediasource.codec.MediaSourceArguments
import me.him188.ani.app.domain.mediasource.codec.MediaSourceCodecManager
import me.him188.ani.app.domain.mediasource.subscription.MediaSourceSubscription.UpdateError
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.platform.Uuid
import me.him188.ani.utils.platform.currentTimeMillis
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class MediaSourceSubscriptionUpdater(
    private val subscriptions: MediaSourceSubscriptionRepository,
    private val mediaSourceManager: MediaSourceManager,
    private val codecManager: MediaSourceCodecManager,
    private val requestSubscription: suspend (MediaSourceSubscription) -> ApiResponse<SubscriptionUpdateData>,
) {
    /**
     * @param force to ignore lastUpdated time
     * @return delay duration to check next time
     */
    suspend fun updateAllOutdated(force: Boolean = false): Duration {
        logger.info { "MediaSourceSubscriptionUpdater.updateAllOutdated" }
        val subscriptions = subscriptions.flow.first()
        val currentTimeMillis = currentTimeMillis()

        for (subscription in subscriptions) {
            fun shouldUpdate(): Boolean {
                if (force) return true
                if (subscription.lastUpdated == null) return true
                return (currentTimeMillis - subscription.lastUpdated.timeMillis).milliseconds > subscription.updatePeriod
            }

            if (!shouldUpdate()) {
                continue
            }

            logger.info { "Updating subscription: ${subscription.url}" }

            kotlin.runCatching {
                updateSubscription(subscription)
            }.fold(
                onSuccess = { count ->
                    this.subscriptions.update(subscription.subscriptionId) { old ->
                        old.copy(
                            lastUpdated = MediaSourceSubscription.LastUpdated(
                                currentTimeMillis,
                                mediaSourceCount = count,
                                error = null,
                            ),
                        )
                    }
                },
                onFailure = { error ->
                    logger.error(error) { "Failed to update subscription ${subscription.url}" }
                    this.subscriptions.update(subscription.subscriptionId) { old ->
                        old.copy(
                            lastUpdated = MediaSourceSubscription.LastUpdated(
                                currentTimeMillis,
                                mediaSourceCount = null,
                                error = UpdateError(error.toString()),
                            ),
                        )
                    }
                },
            )
        }

        return subscriptions.minOf { subscription -> subscription.updatePeriod }
    }

    data class ExistingArgument(
        val save: MediaSourceSave,
        val arguments: MediaSourceArguments?,
    )

    class NewArgument(
        val data: ExportedMediaSourceData,
        val deserializedArguments: MediaSourceArguments,
    ) {
        val name get() = deserializedArguments.name
        val factoryId get() = data.factoryId

    }

    @Throws(UpdateSubscriptionException::class, CancellationException::class)
    private suspend fun updateSubscription(subscription: MediaSourceSubscription): Int {
        val updateData = requestSubscription(subscription).valueOrElse {
            throw RequestFailureException(it)
        }

        val newArguments = updateData.exportedMediaSourceDataList.mediaSources.mapNotNull {
            runCatching {
                NewArgument(it, codecManager.decode(it))
            }.getOrNull()
        }

        val existing = mediaSourceManager.getListBySubscriptionId(subscriptionId = subscription.subscriptionId)
            .map { save ->
                ExistingArgument(save, deserializeArgumentsOrNull(save))
            }

        val diff = calculateDiff(newArguments, existing)
        logger.info { "updateSubscription diff: $diff" }

        for ((save, _) in diff.removed) {
            mediaSourceManager.removeInstance(save.instanceId)
        }

        for (argument in diff.added) {
            val id = Uuid.randomString()
            mediaSourceManager.addInstance(
                id,
                id,
                argument.factoryId,
                MediaSourceConfig(
                    serializedArguments = argument.data.arguments,
                    subscriptionId = subscription.subscriptionId,
                ),
            )
        }

        for ((existing, new) in diff.changed) {
            if (!mediaSourceManager.updateMediaSourceArguments(existing.save.instanceId, new.data.arguments)) {
                logger.error { "Failed to update existing save ${existing.save.instanceId}" }
            }
        }

        return updateData.exportedMediaSourceDataList.mediaSources.size
    }

    private fun deserializeArgumentsOrNull(save: MediaSourceSave): MediaSourceArguments? {
        return save.config.serializedArguments?.let {
            try {
                codecManager.deserializeArgument(save.factoryId, it)
            } catch (e: IllegalArgumentException) {
                throw e
            }
        }
    }

    data class Diff(
        val removed: List<ExistingArgument>,
        val added: List<NewArgument>,
        val changed: List<Pair<ExistingArgument, NewArgument>>,
    ) {
        override fun toString(): String {
            return "Diff(removed=${removed.joinToString()}"
        }
    }

    companion object {
        val logger = logger<MediaSourceSubscriptionUpdater>()

        fun calculateDiff(newArguments: List<NewArgument>, existing: List<ExistingArgument>): Diff {
            val removed = existing.filter { (save, local) ->
                // 新到的里面不包含这个, 说明这个被删除了
                newArguments.none { it.name == local?.name }
            }

            val added = newArguments.filter { it ->
                existing.none { (_, args) -> it.name == args?.name }
            }

            val changed = newArguments.mapNotNull { new ->
                val exi = existing.find { (_, args) -> new.name == args?.name }
                    ?: return@mapNotNull null
                exi to new
            }
            return Diff(removed, added, changed)
        }
    }
}

sealed class UpdateSubscriptionException(override val message: String?) : Exception()
class RequestFailureException(apiFailure: ApiFailure) : UpdateSubscriptionException("Request failed: $apiFailure")
