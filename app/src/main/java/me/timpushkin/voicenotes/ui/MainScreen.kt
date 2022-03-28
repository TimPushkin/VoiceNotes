package me.timpushkin.voicenotes.ui

import android.text.format.DateUtils
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.timpushkin.voicenotes.ApplicationState
import me.timpushkin.voicenotes.R
import me.timpushkin.voicenotes.models.Recording

@Composable
fun MainScreen(
    applicationState: ApplicationState,
    onRename: (Recording, String) -> Unit,
    onPlay: (Recording) -> Unit,
    onPause: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        floatingActionButton = {
            RecordButton(
                isRecording = applicationState.isRecording,
                onClick = {
                    if (applicationState.isRecording) onStopRecording()
                    else onStartRecording()
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        backgroundColor = MaterialTheme.colors.background
    ) { contentPadding ->
        Crossfade(applicationState.isRecording) { isRecording ->
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize()
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = DateUtils
                            .formatElapsedTime(applicationState.recordingTime / 1000),
                        maxLines = 1,
                        style = MaterialTheme.typography.h1
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(start = 20.dp, top = 20.dp, end = 20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.your_recordings),
                        style = MaterialTheme.typography.h4
                    )

                    RecordingsList(
                        recordings = applicationState.recordings.values.toList(),
                        nowPlaying = applicationState.nowPlaying,
                        onRename = onRename,
                        onPlay = { recording ->
                            if (applicationState.nowPlaying == recording) onPause()
                            else onPlay(recording)
                        },
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }

    key(applicationState.snackbarContent) {
        applicationState.snackbarContent?.let { (text, label, action) ->
            scope.launch {
                if (scaffoldState.snackbarHostState.showSnackbar(
                        text,
                        label
                    ) == SnackbarResult.ActionPerformed
                ) action()
            }
        }
    }
}
