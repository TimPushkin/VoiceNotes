package me.timpushkin.voicenotes.ui

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
    nowPlaying: Recording,
    onRename: (Recording, String) -> Unit,
    onPlay: (Recording) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(recordings) { recording ->
            val isPlaying = recording == nowPlaying

            RecordingCard(
                name = recording.name,
                date = recording.date,
                duration = recording.duration,
                isPlaying = isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                played = recording.played,
                onRename = { name -> onRename(recording, name) },
                onPlay = { onPlay(recording) }
            )
        }
    }
}
