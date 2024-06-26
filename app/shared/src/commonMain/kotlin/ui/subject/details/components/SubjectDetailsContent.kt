package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


object SubjectDetailsDefaults

@Composable
fun SubjectDetailsDefaults.DetailsTab(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    Column(
        modifier
            .padding(vertical = 16.dp)
            .fillMaxSize(),
    ) {
        repeat(50) {
            Text(
                "演职人员",
                Modifier.padding(horizontal = horizontalPadding),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
//    LazyColumn(
//        modifier
//            .padding(vertical = 16.dp)
//            .fillMaxSize(),
//    ) {
//        items(50) {
//            Text(
//                "演职人员",
//                Modifier.padding(horizontal = horizontalPadding),
//                style = MaterialTheme.typography.titleMedium,
//            )
//        }
//    }
}
