package me.him188.ani.app.ui.framework

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.captureToImage
import kotlinx.io.files.SystemTemporaryDirectory
import me.him188.ani.test.readTestResourceAsByteArray
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.createDirectories
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.io.resolve
import me.him188.ani.utils.io.writeBytes
import me.him188.ani.utils.platform.Uuid
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.impl.use
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.fail

/**
 * 截图当前的 UI 并与 resources 目录下的图片 [expectedResource] 进行比较.
 */
fun SkikoComposeUiTest.assertScreenshot(expectedResource: String) {
    captureToImage().assertScreenshot(expectedResource)
}

/**
 * 截图当前的 UI 并与 resources 目录下的图片 [expectedResource] 进行比较.
 */
fun SemanticsNodeInteraction.assertScreenshot(expectedResource: String) {
    captureToImage().assertScreenshot(expectedResource)
}

/**
 * 截图当前的 UI 并与 resources 目录下的图片 [expectedResource] 进行比较.
 */
@OptIn(ExperimentalEncodingApi::class)
actual fun ImageBitmap.assertScreenshot(expectedResource: String) {
    // https://github.com/JetBrains/compose-multiplatform-core/blob/jb-main/compose/ui/ui-test-junit4/src/desktopTest/kotlin/androidx/compose/ui/test/UseComposeUiTest.kt

    Image.makeFromBitmap(asSkiaBitmap()).use { img: Image ->
        val actualImage = img.encodeToData(EncodedImageFormat.PNG)
            ?: error("Could not encode image as png")
        val actualBytes = actualImage.bytes
        val tempDir = SystemTemporaryDirectory.inSystem.resolve("ani-compose-test")
        if (expectedResource.isEmpty()) {
            val tempFile = saveActualImage(actualBytes, tempDir)
            fail(
                """
                No expected screenshot provided. 
                Actual screenshot saved to file://$tempFile
                    into directory file://$tempDir
                You can update the expected image by copying the actual image to the resources directory. The resource directory is normally located at `src/skikoTest/resources/`.
                For your convenience, you can copy:
                assertScreenshot("/screenshots/${tempFile.path.name}")
                """.trimIndent(),
            )
        }

        val expectedBytes = readTestResourceAsByteArray(expectedResource)
        if (actualBytes.contentEquals(expectedBytes)) {
            // ok
        } else {
            val tempFile = saveActualImage(actualBytes, tempDir)
            fail(
                """
                    The actual screenshot does not match the expected screenshot. 
                    Actual image saved as file://$tempFile
                      into directory file://$tempDir
                    Actual image Base64: 
                    ${Base64.encode(actualBytes)}
                    You should check if the actual image is correct. If it is, you can update the expected image by copying the actual image to the resources directory, overwriting `$expectedResource`. The resource directory is normally located at `src/skikoTest/resources/`.
                    For your convenience, you can copy:
                    assertScreenshot("/screenshots/${tempFile.path.name}")
                    """.trimIndent(),

                )
        }
    }
}

private fun saveActualImage(
    actualBytes: ByteArray,
    folder: SystemPath,
): SystemPath {
    folder.createDirectories()
    val tempFile = folder.resolve(
        (Exception().guessTestFunctionName() ?: "ani-compose-test-${Uuid.randomString()}") + ".png",
    )
    // 添加测试时获取截图
    tempFile.writeBytes(actualBytes)
    return tempFile
}

internal expect fun Throwable.guessTestFunctionName(): String?