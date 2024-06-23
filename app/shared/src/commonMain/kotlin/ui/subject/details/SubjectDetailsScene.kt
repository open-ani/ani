package me.him188.ani.app.ui.subject.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SubjectDetailsScene(
    vm: SubjectDetailsViewModel,
) {
    Column(
        Modifier
            .fillMaxSize(),
    ) {
        SubjectDetailsPage(vm)
    }
}