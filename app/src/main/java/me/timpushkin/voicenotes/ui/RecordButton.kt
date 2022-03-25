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
fun RecordButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_round_mic_none_24),
            contentDescription = "Record",
            tint = MaterialTheme.colors.surface
        )
    }
}

@Preview
@Composable
fun RecordButtonPreview() {
    VoiceNotesTheme {
        RecordButton({})
    }
}
