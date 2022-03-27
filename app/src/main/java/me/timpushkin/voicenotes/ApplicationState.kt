package me.timpushkin.voicenotes

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.timpushkin.voicenotes.controllers.Player
import me.timpushkin.voicenotes.controllers.Recorder
import me.timpushkin.voicenotes.models.Recording
import me.timpushkin.voicenotes.models.SnackbarContent
import java.io.FileDescriptor

class ApplicationState : ViewModel() {
    private val player = Player()
    private val recorder = Recorder()

    private var _nowPlaying by mutableStateOf<Uri?>(null)
    val nowPlaying: Uri?
        get() = _nowPlaying

    private var _isRecording by mutableStateOf(false)
    val isRecording: Boolean
        get() = _isRecording

    private var _recordings by mutableStateOf(emptyList<Recording>())
    val recordings: List<Recording>
        get() = _recordings

    private var _snackbarContent by mutableStateOf<SnackbarContentImpl?>(null)
    val snackbarContent: SnackbarContent?
        get() = _snackbarContent

    fun setRecordingsWith(getRecordings: () -> List<Recording>) {
        viewModelScope.launch { _recordings = getRecordings() }
    }

    fun startPlaying(recordingUri: Uri, fd: FileDescriptor, onError: () -> Unit = {}) {
        player.start(
            input = fd,
            onStarted = { _nowPlaying = recordingUri },
            onCompleted = { _nowPlaying = null },
            onError = {
                _nowPlaying = null
                onError()
            }
        )
    }

    fun stopPlaying() {
        player.stop()
        _nowPlaying = null
    }

    fun startRecording(context: Context, fd: FileDescriptor, onError: () -> Unit = {}) {
        val started = recorder.start(context, fd)
        _isRecording = started
        if (!started) onError()
    }

    fun stopRecording() {
        recorder.stop()
        _isRecording = false
    }

    fun showSnackbar(message: String, label: String? = null, action: () -> Unit = {}) {
        _snackbarContent =
            SnackbarContentImpl(message, label, action, _snackbarContent?.run { id + 1 } ?: 0)
    }

    data class SnackbarContentImpl(
        override val message: String,
        override val label: String? = null,
        override val action: () -> Unit = {},
        val id: Int
    ) : SnackbarContent
}
