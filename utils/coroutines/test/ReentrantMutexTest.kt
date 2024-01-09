package me.him188.ani.utils.coroutines

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class ReentrantMutexTest {

    private val mutex = ReentrantMutex()

    @Test
    fun canLock() = runTest {
        mutex.withLock {
        }
    }

    @Test
    fun mutualExclusion() = runTest {
        var v = 1
        launch(start = CoroutineStart.UNDISPATCHED) {
            mutex.withLock {
                v = 2
                yield()
            }
        }
        launch(start = CoroutineStart.UNDISPATCHED) {
            mutex.withLock {
                yield()
                assertEquals(2, v)
            }
        }
    }

    @Test
    fun reentrant() = runTest {
        mutex.withLock {
            mutex.withLock {
                yield()
            }
        }
    }

    @Test
    fun reentrantAgain() = runTest {
        mutex.withLock {
            mutex.withLock {
                yield()
            }
            mutex.withLock {
                yield()
            }
        }
    }

    @Test
    fun exception() = runTest {
        assertThrows<NoSuchElementException> {
            mutex.withLock {
                throw NoSuchElementException()
            }
        }
        mutualExclusion()
    }

    @Test
    fun exceptionInNested() = runTest {
        assertThrows<NoSuchElementException> {
            mutex.withLock {
                mutex.withLock {
                    throw NoSuchElementException()
                }
            }
        }
        mutualExclusion()
    }
}