package me.him188.ani.utils.io

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextLong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BufferedFileInputTest {

    @TempDir
    private lateinit var tempDir: File

    private lateinit var file: File
    private val bufferSize = 20
    private val input: BufferedFileInput by lazy { file.toSeekableInput(bufferSize) as BufferedFileInput }
    private val sampleText =
        "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."

    @BeforeEach
    fun init() {
        val f = File(tempDir, "test")
        f.writeText(sampleText)
        file = f
    }

    @AfterEach
    fun cleanup() {
        input.close()
    }

    @Test
    fun `read sequentially once`() {
        assertEquals(sampleText, input.readAllBytes().decodeToString())
    }

    @Test
    fun `read sequentially multiple`() {
        assertEquals(sampleText.take(4), input.readBytes(maxLength = 4).decodeToString())
        assertEquals(sampleText.drop(4).take(4), input.readBytes(maxLength = 4).decodeToString())
        assertEquals(sampleText.drop(8), input.readAllBytes().decodeToString())
    }

    @Test
    fun `seek then read fully`() {
        input.seek(4)
        assertEquals(sampleText.drop(4), input.readAllBytes().decodeToString())
    }

    @Test
    fun `read, seek forward, read fully`() {
        assertEquals(sampleText.take(4), input.readBytes(maxLength = 4).decodeToString())
        input.seek(8)
        assertEquals(sampleText.drop(8), input.readAllBytes().decodeToString())
    }

    @Test
    fun `read, seek back, read fully`() {
        assertEquals(sampleText.take(4), input.readBytes(maxLength = 4).decodeToString())
        input.seek(0)
        assertEquals(sampleText, input.readAllBytes().decodeToString())
    }

    @Test
    fun `double seek same`() {
        assertEquals(sampleText.take(4), input.readBytes(maxLength = 4).decodeToString())
        input.seek(0)
        input.seek(0)
        assertEquals(sampleText, input.readAllBytes().decodeToString())
    }

    @Test
    fun `seek forward then back`() {
        assertEquals(sampleText.take(4), input.readBytes(maxLength = 4).decodeToString())
        input.seek(8)
        input.seek(4)
        assertEquals(sampleText.drop(4), input.readAllBytes().decodeToString())
    }

    @Test
    fun `seek over length, read return -1`() {
        input.seek(999999)
        assertEquals(-1, input.read(ByteArray(1), 0, 1))
    }

    @Test
    fun `seek over length, bytesRemaining is zero`() {
        input.seek(999999)
        assertEquals(0, input.bytesRemaining)
    }

    @Test
    fun `seek over length, readBytes return empty`() {
        input.seek(999999)
        assertEquals(0, input.readAllBytes().size)
    }


    ///////////////////////////////////////////////////////////////////////////
    // Reuse buffer
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `reuse buffer from previous start`() {
        // buffer size is 20

        input.seek(30)
        assertEquals(1, input.read(ByteArray(1))) // fill buffer
        assertEquals(30 - bufferSize..<30L + bufferSize, input.bufferedOffsetRange)
        // 10..<50

        input.seek(0) // 超出 buffer 范围
        input.prepareBuffer()
        assertEquals(0L..<bufferSize, input.bufferedOffsetRange)
        // 0..<20, last 10 was reused from previous buffer

        assertEquals("Lorem Ipsum is simpl", input.readExactBytes(20).decodeToString())
    }

    @Test
    fun `reuse buffer from previous end - second half`() {
        // buffer size is 20

        input.seek(30)
        assertEquals(1, input.read(ByteArray(1))) // fill buffer
        assertEquals(30 - bufferSize..<30L + bufferSize, input.bufferedOffsetRange)
        // 10..<50

        input.seek(60) // 超出 buffer 范围
        input.prepareBuffer()
        assertEquals(60 - bufferSize..<60L + bufferSize, input.bufferedOffsetRange)
        // 40..<80, first 10 was reused from previous buffer

        assertEquals(sampleText.substring(60..<70), input.readExactBytes(10).decodeToString())
    }

    @Test
    fun `reuse buffer from previous end - first half`() {
        // buffer size is 20

        input.seek(30)
        assertEquals(1, input.read(ByteArray(1))) // fill buffer
        assertEquals(30 - bufferSize..<30L + bufferSize, input.bufferedOffsetRange)
        // 10..<50

        input.seek(50) // 超出 buffer 范围
        input.prepareBuffer()
        assertEquals(50 - bufferSize..<50L + bufferSize, input.bufferedOffsetRange)
        // 40..<80, first 10 was reused from previous buffer

        assertEquals(sampleText.substring(50..<60), input.readExactBytes(10).decodeToString())
    }

    @Test
    fun `random seek and read`() {
        val random = Random(2352151)
        repeat(1000) {
            val pos = random.nextLong(0L..<sampleText.length).absoluteValue
            input.seek(pos)
//                val length = Random.nextInt()
            assertEquals(sampleText.substring(pos.toInt()), input.readAllBytes().decodeToString())
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Error cases
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `seek negative fails`() {
        assertFailsWith<IllegalArgumentException> {
            input.seek(-1)
        }
    }

    @Test
    fun `read negative length fails`() {
        assertFailsWith<IllegalArgumentException> {
            input.read(ByteArray(1), 1, -1)
        }
    }

    @Test
    fun `read negative offset fails`() {
        assertFailsWith<IllegalArgumentException> {
            input.read(ByteArray(1), -1, 1)
        }
    }
}