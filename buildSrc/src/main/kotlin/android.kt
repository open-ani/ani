import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project


fun Project.configureAndroidLibrary(
    namespace: String,
    composeCompilerVersion: String,
) {
    check(namespace.startsWith("me.him188.ani"))
    (extensions.getByName("android") as LibraryExtension).apply {
        this.namespace = namespace
        compileSdk = getIntProperty("android.compile.sdk")
        defaultConfig {
            minSdk = getIntProperty("android.min.sdk")
            buildConfigField("String", "VERSION_NAME", "\"${getProperty("version.name")}\"")
        }
        buildTypes.getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                *sharedAndroidProguardRules(),
            )
        }
        buildFeatures {
            compose = true
            buildConfig = true
        }
        composeOptions {
            kotlinCompilerExtensionVersion = composeCompilerVersion
        }
    }
}