package me.timpushkin.voicenotes

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.timpushkin.voicenotes.models.Recording
import me.timpushkin.voicenotes.models.SnackbarContent
import me.timpushkin.voicenotes.utils.CountingTimer

class ApplicationState : ViewModel() {
    private var timer = CountingTimer()

    var nowPlaying by mutableStateOf<Uri?>(null)
    var playerPosition by mutableStateOf(0)
    var isRecording by mutableStateOf(false)

    private var _recordingTime by mutableStateOf(0L)
    val recordingTime: Long
        get() = _recordingTime

    private var _recordings by mutableStateOf(emptyList<Recording>())
    val recordings: List<Recording>
        get() = _recordings

    private var _snackbarContent by mutableStateOf<SnackbarContentImpl?>(null)
    val snackbarContent: SnackbarContent?
        get() = _snackbarContent

    fun setRecordingsWith(getRecordings: () -> List<Recording>) {
        viewModelScope.launch { _recordings = getRecordings() }
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
