@file:Suppress("ACTUAL_CLASSIFIER_MUST_HAVE_THE_SAME_MEMBERS_AS_NON_FINAL_EXPECT_CLASSIFIER_WARNING")

package me.him188.ani.app.ui.foundation.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) = androidx.activity.compose.BackHandler(enabled, onBack)

actual typealias LocalOnBackPressedDispatcherOwner = androidx.activity.compose.LocalOnBackPressedDispatcherOwner

actual typealias OnBackPressedDispatcherOwner = androidx.activity.OnBackPressedDispatcherOwner

actual typealias OnBackPressedDispatcher = androidx.activity.OnBackPressedDispatcher
