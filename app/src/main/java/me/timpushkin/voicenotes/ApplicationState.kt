package me.timpushkin.voicenotes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.timpushkin.voicenotes.models.Recording
import me.timpushkin.voicenotes.models.SnackbarContent
import me.timpushkin.voicenotes.utils.CountingTimer

class ApplicationState : ViewModel() {
    private var timer = CountingTimer()
    private var ioScope = CoroutineScope(Dispatchers.IO)

    var nowPlaying by mutableStateOf(Recording.EMPTY)
    var isRecording by mutableStateOf(false)

    private var _recordingTime by mutableStateOf(0L)
    val recordingTime: Long
        get() = _recordingTime

    private var _recordings by mutableStateOf(emptyMap<String, Recording>())
    val recordings: Map<String, Recording>
        get() = _recordings

    private var _snackbarContent by mutableStateOf<SnackbarContentImpl?>(null)
    val snackbarContent: SnackbarContent?
        get() = _snackbarContent

    fun setRecordingsWith(getRecordings: () -> List<Recording>) {
        ioScope.launch {
            val newRecordings = mutableMapOf<String, Recording>()
            getRecordings().forEach { recording ->
                val key = recording.uri.toString()
                newRecordings[key] = _recordings[key] ?: recording
            }
            _recordings = newRecordings
        }
    }

    fun startRecordingTimer() {
        timer.start(500L) { _recordingTime = it }
    }

    fun stopRecordingTimer() {
        timer.pause()
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
