import org.gradle.api.Project
import java.io.File
import java.util.Properties

fun Project.getProperty(name: String) =
    getPropertyOrNull(name) ?: error("Property $name not found")

fun Project.getPropertyOrNull(name: String) =
    System.getProperty(name)
        ?: System.getenv(name)
        ?: properties[name]?.toString()
        ?: getLocalProperty(name)
        ?: extensions.extraProperties.runCatching { get(name).toString() }.getOrNull()


val Project.localPropertiesFile: File get() = project.rootProject.file("local.properties")

fun Project.getLocalProperty(key: String): String? {
    return if (localPropertiesFile.exists()) {
        val properties = Properties()
        localPropertiesFile.inputStream().buffered().use { input ->
            properties.load(input)
        }
        properties.getProperty(key)
    } else {
        localPropertiesFile.createNewFile()
        null
    }
}


fun Project.getIntProperty(name: String) = getProperty(name).toInt()

val Project.enableAnitorrent
    get() = (getPropertyOrNull("ani.enable.anitorrent") ?: "false").toBooleanStrict()

val Project.enableIos
    get() = getPropertyOrNull("ani.enable.ios")?.toBooleanStrict() ?: true
