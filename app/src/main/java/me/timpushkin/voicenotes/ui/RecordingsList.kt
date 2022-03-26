package me.timpushkin.voicenotes.ui

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timpushkin.voicenotes.models.Recording

@Composable
fun RecordingsList(
    recordings: List<Recording>,
    onElementClick: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(10.dp)
    ) {
        items(recordings) { recording ->
            RecordingCard(
                name = recording.name,
                date = recording.date,
                duration = recording.duration,
                onClick = { onElementClick(recording.uri) }
            )
        }
    }
}
