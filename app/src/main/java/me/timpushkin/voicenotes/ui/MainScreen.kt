package me.timpushkin.voicenotes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.timpushkin.voicenotes.R
import me.timpushkin.voicenotes.RecordingsViewModel

@Composable
fun MainScreen(
    recordingsViewModel: RecordingsViewModel,
    onPlay: (Long) -> Unit,
    onRecord: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Text(
                    text = stringResource(R.string.your_recordings),
                    style = MaterialTheme.typography.h3
                )

                RecordingsList(
                    recordingsViewModel = recordingsViewModel,
                    onElementClick = onPlay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
            }

            RecordButton(
                onClick = onRecord,
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
