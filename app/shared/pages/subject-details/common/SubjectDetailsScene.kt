package me.him188.ani.app.ui.subject.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.navigation.Navigator

@Composable
fun SubjectDetailsScene(
    vm: SubjectDetailsViewModel,
    navigator: Navigator,
) {
    Column(
        Modifier
            .fillMaxSize()
    ) {
        SubjectDetailsPage(vm) { navigator.goBack() }
    }
}