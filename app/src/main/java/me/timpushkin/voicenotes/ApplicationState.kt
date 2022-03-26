package me.timpushkin.voicenotes

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import me.timpushkin.voicenotes.controllers.Recorder
import me.timpushkin.voicenotes.models.Recording
import me.timpushkin.voicenotes.models.SnackbarContent
import java.io.FileDescriptor

class ApplicationState : ViewModel() {
    var isRecording by mutableStateOf(false)
    val recordings by mutableStateOf(emptyList<Recording>())

    private var _snackbarContent by mutableStateOf<SnackbarContentImpl?>(null)
    val snackbarContent: SnackbarContent?
        get() = _snackbarContent

    private val recorder = Recorder()

    fun startRecording(context: Context, fd: FileDescriptor) {
        isRecording = recorder.start(context, fd)
    }

    fun stopRecording() {
        recorder.stop()
        isRecording = false
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
