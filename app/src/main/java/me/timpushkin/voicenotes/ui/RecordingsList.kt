package me.timpushkin.voicenotes.ui

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
    onElementClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(10.dp)
    ) {
        items(recordings) { recording ->
            RecordingCard(
                title = recording.title,
                timestamp = recording.timestamp,
                durationSec = recording.duration,
                onClick = { onElementClick(recording.id) }
            )
        }
    }
}
