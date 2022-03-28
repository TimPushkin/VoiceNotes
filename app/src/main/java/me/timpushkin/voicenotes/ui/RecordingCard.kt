package me.timpushkin.voicenotes.ui

import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    onClick: () -> Unit = {}
) {
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
                RecordingTextInfo(name, date, Modifier.weight(1f))

                Spacer(modifier = Modifier.width(10.dp))

                RecordingTimeInfo(duration, played)

                Spacer(modifier = Modifier.width(10.dp))

                PlayButton(isPlaying, onClick)
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
}

@Composable
fun RecordingTextInfo(name: String, date: Long, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = name,
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
            style = MaterialTheme.typography.caption.run { copy(color = color.copy(alpha = 0.7f)) }
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
        style = MaterialTheme.typography.caption.run { copy(color = color.copy(alpha = 0.7f)) }
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
