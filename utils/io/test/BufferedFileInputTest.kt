package me.him188.ani.utils.io

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BufferedFileInputTest {

    @TempDir
    private lateinit var tempDir: File

    private lateinit var file: File
    private val input: BufferedFileInput by lazy { file.asSeekableInput() as BufferedFileInput }
    private val expectedText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam nec nunc nec nunc."

    @BeforeEach
    fun init() {
        val f = File(tempDir, "test")
        f.writeText(expectedText)
        file = f
    }

    @AfterEach
    fun cleanup() {
        input.close()
    }

    @Test
    fun `read sequentially once`() = runTest {
        assertEquals(expectedText, input.readBytes().decodeToString())
    }

    @Test
    fun `read sequentially multiple`() = runTest {
        assertEquals(expectedText.take(4), input.readBytes(maxLength = 4).decodeToString())
        assertEquals(expectedText.drop(4).take(4), input.readBytes(maxLength = 4).decodeToString())
        assertEquals(expectedText.drop(8), input.readBytes().decodeToString())
    }

    @Test
    fun `seek then read fully`() = runTest {
        input.seek(4)
        assertEquals(expectedText.drop(4), input.readBytes().decodeToString())
    }

    @Test
    fun `read, seek forward, read fully`() = runTest {
        assertEquals(expectedText.take(4), input.readBytes(maxLength = 4).decodeToString())
        input.seek(8)
        assertEquals(expectedText.drop(8), input.readBytes().decodeToString())
    }

    @Test
    fun `read, seek back, read fully`() = runTest {
        assertEquals(expectedText.take(4), input.readBytes(maxLength = 4).decodeToString())
        input.seek(0)
        assertEquals(expectedText, input.readBytes().decodeToString())
    }

    @Test
    fun `double seek same`() = runTest {
        assertEquals(expectedText.take(4), input.readBytes(maxLength = 4).decodeToString())
        input.seek(0)
        input.seek(0)
        assertEquals(expectedText, input.readBytes().decodeToString())
    }

    @Test
    fun `double seek forward then back`() = runTest {
        assertEquals(expectedText.take(4), input.readBytes(maxLength = 4).decodeToString())
        input.seek(8)
        input.seek(4)
        assertEquals(expectedText.drop(4), input.readBytes().decodeToString())
    }

    @Test
    fun `seek over length, read return -1`() = runTest {
        input.seek(999999)
        assertEquals(-1, input.read(ByteArray(1), 0, 1))
    }

    @Test
    fun `seek over length, readBytes return empty`() = runTest {
        input.seek(999999)
        assertEquals(0, input.readBytes().size)
    }


    @Test
    fun `stream not created on init`() = runTest {
        assertEquals(0, input.streamCounter)
    }

    @Test
    fun `stream lazily created`() = runTest {
        input.readBytes()
        assertEquals(1, input.streamCounter)
    }

    @Test
    fun `stream created on seek back`() = runTest {
        input.readBytes()
        assertEquals(1, input.streamCounter)
        input.seek(1)
        assertEquals(2, input.streamCounter)
    }

    @Test
    fun `stream created once if seek without read`() = runTest {
        input.seek(1)
        assertEquals(1, input.streamCounter)
    }


    @Test
    fun `seek negative fails`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            input.seek(-1)
        }
    }

    @Test
    fun `read negative length fails`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            input.read(ByteArray(1), 1, -1)
        }
    }

    @Test
    fun `read negative offset fails`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            input.read(ByteArray(1), -1, 1)
        }
    }
}