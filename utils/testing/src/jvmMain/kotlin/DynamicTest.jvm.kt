package me.him188.ani.test

actual typealias TestFactory = org.junit.jupiter.api.TestFactory

actual typealias DynamicTest = org.junit.jupiter.api.DynamicTest

// 不要用 DynamicContainer.dynamicContainer, 因为它会导致 test 报告多一层
/**
 * @see TestFactory
 */
class DynamicTestContainer(
    val collection: Iterable<DynamicTest>
) : Iterable<DynamicTest> by collection

actual typealias DynamicTestsResult = DynamicTestContainer

actual fun dynamicTest(displayName: String, action: () -> Unit): DynamicTest {
    return DynamicTest.dynamicTest(displayName, action)
}

actual fun runDynamicTests(dynamicTests: List<DynamicTest>): DynamicTestsResult =
    DynamicTestContainer(dynamicTests)
