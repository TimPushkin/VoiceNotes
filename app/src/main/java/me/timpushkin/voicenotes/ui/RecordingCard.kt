package me.timpushkin.voicenotes.ui

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.timpushkin.voicenotes.R
import me.timpushkin.voicenotes.ui.theme.VoiceNotesTheme

@Composable
fun RecordingCard(
    name: String,
    date: Long,
    duration: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    played: Int = 0,
    onRename: (String) -> Unit = {},
    onPlay: () -> Unit = {}
) {
    var showRenameDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        elevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RecordingTextInfo(name, date, Modifier.weight(1f)) { showRenameDialog = true }

                Spacer(modifier = Modifier.width(10.dp))

                RecordingTimeInfo(duration, played)

                Spacer(modifier = Modifier.width(10.dp))

                PlayButton(isPlaying, onPlay)
            }

            LinearProgressIndicator(
                progress = played.toFloat() / duration,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                backgroundColor = MaterialTheme.colors.surface
            )
        }
    }

    if (showRenameDialog) {
        RenameDialog(
            initialName = name,
            onDismiss = { showRenameDialog = false },
            onApply = { newName ->
                onRename(newName)
                showRenameDialog = false
            }
        )
    }
}

@Composable
fun RenameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit = {}
) {
    var name by remember { mutableStateOf(initialName) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    label = { Text(stringResource(R.string.rename_recording_caption)) }
                )
                Button(
                    onClick = { if (name != initialName && name.isNotBlank()) onApply(name) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
    }
}

@Composable
fun RecordingTextInfo(
    name: String,
    date: Long,
    modifier: Modifier = Modifier,
    onNameClick: () -> Unit = {}
) {
    Column(modifier = modifier) {
        Text(
            text = name,
            modifier = Modifier.clickable(onClick = onNameClick),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.h6
        )

        Text(
            text = DateUtils.formatDateTime(
                LocalContext.current,
                date,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
            ),
            maxLines = 1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.caption
        )
    }
}

@Composable
fun RecordingTimeInfo(duration: Int, played: Int, modifier: Modifier = Modifier) {
    val durationStr = DateUtils.formatElapsedTime(duration.toLong() / 1000)
    val text =
        if (played > 0) "${DateUtils.formatElapsedTime(played.toLong() / 1000)} / $durationStr"
        else durationStr

    Text(
        text = text,
        modifier = modifier,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
        style = MaterialTheme.typography.caption
    )
}

@Composable
fun PlayButton(isPlaying: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .requiredSize(35.dp)
            .then(modifier),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isPlaying) MaterialTheme.colors.secondary else MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.surface
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        if (isPlaying) {
            Icon(
                painter = painterResource(R.drawable.ic_round_pause_24),
                contentDescription = "Pause",
                tint = MaterialTheme.colors.surface
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_round_play_arrow_24),
                contentDescription = "Play",
                tint = MaterialTheme.colors.surface
            )
        }
    }
}

@Preview
@Composable
fun RecordingCardWhenNotPlayingPreview() {
    VoiceNotesTheme {
        RecordingCard(
            name = "Title",
            date = 9999999999999,
            isPlaying = false,
            duration = 10000
        )
    }
}

@Preview
@Composable
fun RecordingCardWhenPlayingPreview() {
    VoiceNotesTheme {
        RecordingCard(
            name = "Title",
            date = 9999999999999,
            duration = 10000,
            isPlaying = true,
            played = 7000
        )
    }
}

@Preview
@Composable
fun RecordingCardWithLongTitlePreview() {
    VoiceNotesTheme {
        RecordingCard(
            name = "A recording with a very long title",
            date = 9999999999999,
            duration = 10000,
            isPlaying = true,
            played = 7000
        )
    }
}

@Preview
@Composable
fun RenameDialogPreview() {
    RenameDialog(
        initialName = "Some name",
        onDismiss = {}
    )
}
