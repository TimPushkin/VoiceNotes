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
    duration: Long,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.65f)) {
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

            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = DateUtils.formatElapsedTime(duration),
                    maxLines = 1,
                    style = MaterialTheme.typography.caption.run { copy(color = color.copy(alpha = 0.7f)) }
                )
            }

            Button(
                onClick = onClick,
                modifier = Modifier
                    .size(35.dp)
                    .requiredSize(35.dp),
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
    }
}

@Preview
@Composable
fun RecordingCardPreview() {
    VoiceNotesTheme {
        RecordingCard(
            name = "Title",
            date = 9999999999999,
            isPlaying = false,
            duration = 10000,
            onClick = {})
    }
}
