package me.him188.ani.app.ui

import androidx.compose.ui.test.SemanticsNodeInteraction

fun SemanticsNodeInteraction.exists(): Boolean {
    try {
        assertDoesNotExist()
    } catch (e: AssertionError) {
        return true
    }
    return false
}

fun SemanticsNodeInteraction.doesNotExist(): Boolean = !exists()
