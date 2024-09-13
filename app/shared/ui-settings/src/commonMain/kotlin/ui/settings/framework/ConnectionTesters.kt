package me.him188.ani.app.ui.settings.framework

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import me.him188.ani.app.tools.MonoTasker
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private inline fun <T> propertyDelegateProvider(
    crossinline createProperty: (property: KProperty<*>) -> T,
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, T>> {
    return PropertyDelegateProvider { _, property ->
        val value = createProperty(property)
        ReadOnlyProperty { _, _ ->
            value
        }
    }
}

/**
 * 创建一个单个的测试器, 需要使用 `val tester by connectionTester {}`
 */
@Suppress("FunctionName")
fun ConnectionTester(
    testConnection: suspend () -> ConnectionTestResult,
    backgroundScope: CoroutineScope,
) = propertyDelegateProvider {
    SingleTester(ConnectionTester(it.name, testConnection), backgroundScope)
}

interface ConnectionTesterRunner<T : Tester<*>> {
    val testers: List<T>

    fun testAll()
    fun cancel()
    fun toggleTest()
    val anyTesting: Boolean
}

@Stable
class SingleTester<T>(
    tester: Tester<T>,
    backgroundScope: CoroutineScope,
) : ConnectionTesterRunner<Tester<T>> by DefaultConnectionTesterRunner(listOf(tester), backgroundScope) {
    val tester get() = testers.single()
}

// 堆屎咯
@Stable
open class DefaultConnectionTesterRunner<T : Tester<*>>(
    override val testers: List<T>,
    backgroundScope: CoroutineScope,
) : ConnectionTesterRunner<T> {
    private val testScope = MonoTasker(backgroundScope)
    override fun testAll() {
        testScope.launch {
            supervisorScope {
                testers.forEach {
                    launch {
                        it.test()
                    }
                }
            }
        }
    }

    override fun cancel() {
        testScope.cancel()
    }

    override fun toggleTest() {
        if (testers.any { it.isTesting }) {
            cancel()
        } else {
            testAll()
        }
    }

    override val anyTesting by derivedStateOf {
        testers.any { it.isTesting }
    }
}
