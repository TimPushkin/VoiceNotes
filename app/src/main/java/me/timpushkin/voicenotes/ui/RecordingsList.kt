package me.timpushkin.voicenotes.ui

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timpushkin.voicenotes.models.Recording

@Composable
fun RecordingsList(
    recordings: List<Recording>,
    nowPlaying: Uri?,
    played: Int,
    onElementClick: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(recordings) { recording ->
            val isPlaying = recording.uri == nowPlaying

            RecordingCard(
                name = recording.name,
                date = recording.date,
                duration = recording.duration,
                isPlaying = isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                played = if (isPlaying) played else 0,
                onClick = { onElementClick(recording.uri) }
            )
        }
    }
}
