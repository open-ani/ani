package me.him188.animationgarden.desktop.app

import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlowImpl
import me.him188.animationgarden.api.impl.model.MutableListFlow
import net.mamoe.yamlkt.Yaml
import java.io.File

@Serializable
data class StarredAnime(
    val name: String,
    val searchQuery: String, // keywords
    val watchedEpisodes: Set<String> = setOf(),
    val preferredAllianceId: String? = null,
    val preferredResolutionId: String? = null,
    val preferredSubtitleId: String? = null,
)


@Stable
class AppData private constructor() {
    @Stable
    val starredAnime: MutableListFlow<StarredAnime> = KeyedMutableListFlowImpl { it.name }

    @Serializable
    private class SerialData(
        val starredAnime: List<StarredAnime> = listOf(),
    )

    companion object {
        fun load(string: String): AppData {
            val decoded = Yaml.decodeFromString(SerialData.serializer(), string)
            return AppData().apply {
                starredAnime.value = decoded.starredAnime
            }
        }

        fun dump(data: AppData): String {
            return Yaml.encodeToString(
                SerialData.serializer(),
                data.run {
                    SerialData(starredAnime.value)
                }
            )
        }
    }
}

class AppDataSaver(
    private val file: File
) {
    lateinit var data: AppData

    fun reload() {
        data = if (!file.exists()) {
            AppData.load("")
        } else {
            AppData.load(file.readText())
        }
    }

    fun save() {
        if (::data.isInitialized) {
            file.writeText(AppData.dump(data))
        }
    }


    @Composable
    fun attachAutoSave() {
        val data by rememberUpdatedState(data)
        val starredAnime by data.starredAnime.asFlow().collectAsState()
        LaunchedEffect(starredAnime) {
            withContext(Dispatchers.IO) {
                save()
            }
        }
    }
}