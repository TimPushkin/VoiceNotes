package me.timpushkin.voicenotes

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.timpushkin.voicenotes.controllers.Player
import me.timpushkin.voicenotes.controllers.Recorder
import me.timpushkin.voicenotes.models.Recording
import me.timpushkin.voicenotes.models.SnackbarContent
import java.io.FileDescriptor
import java.util.concurrent.atomic.AtomicBoolean

class ApplicationState : ViewModel() {
    private val looper = SingleTaskLooper()
    private val player = Player()
    private val recorder = Recorder()

    private var _nowPlaying by mutableStateOf<Uri?>(null)
    val nowPlaying: Uri?
        get() = _nowPlaying

    private var _playerPosition by mutableStateOf(0)
    val playerPosition: Int
        get() = _playerPosition

    private var _isRecording by mutableStateOf(false)
    val isRecording: Boolean
        get() = _isRecording

    private var _recordings by mutableStateOf(emptyList<Recording>())
    val recordings: List<Recording>
        get() = _recordings

    private var _snackbarContent by mutableStateOf<SnackbarContentImpl?>(null)
    val snackbarContent: SnackbarContent?
        get() = _snackbarContent

    private inner class SingleTaskLooper {
        private var isLooping = AtomicBoolean(false)

        fun launch(
            condition: () -> Boolean,
            delay: Long,
            block: suspend CoroutineScope.() -> Unit
        ) {
            if (isLooping.compareAndSet(false, true)) viewModelScope.launch {
                while (condition()) {
                    block()
                    delay(delay)
                }
                isLooping.set(false)
            }
        }
    }

    fun setRecordingsWith(getRecordings: () -> List<Recording>) {
        viewModelScope.launch { _recordings = getRecordings() }
    }

    fun startPlaying(recordingUri: Uri, fd: FileDescriptor, onError: () -> Unit = {}) {
        player.start(
            input = fd,
            onStarted = {
                _nowPlaying = recordingUri
                looper.launch({ nowPlaying != null }, 500) {
                    _playerPosition = player.position
                }
            },
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

    fun stopRecording(): Boolean {
        return recorder.stop().also { _isRecording = false }
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
