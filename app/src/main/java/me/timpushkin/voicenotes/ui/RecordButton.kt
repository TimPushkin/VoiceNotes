package me.timpushkin.voicenotes.ui

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import me.timpushkin.voicenotes.R
import me.timpushkin.voicenotes.ui.theme.VoiceNotesTheme

@Composable
fun RecordButton(isRecording: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.primary
    ) {
        if (isRecording) {
            Icon(
                painter = painterResource(R.drawable.ic_round_stop_24),
                contentDescription = "Stop recording",
                tint = MaterialTheme.colors.surface
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_round_mic_none_24),
                contentDescription = "Record",
                tint = MaterialTheme.colors.surface
            )
        }
    }
}

@Preview
@Composable
fun RecordButtonWhenNotRecordingPreview() {
    VoiceNotesTheme {
        RecordButton(false, {})
    }
}

@Preview
@Composable
fun RecordButtonWhenRecordingPreview() {
    VoiceNotesTheme {
        RecordButton(true, {})
    }
}
