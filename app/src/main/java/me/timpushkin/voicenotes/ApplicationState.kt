package me.timpushkin.voicenotes

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import me.timpushkin.voicenotes.controllers.Recorder
import me.timpushkin.voicenotes.models.Recording
import java.io.FileDescriptor

class ApplicationState : ViewModel() {
    var isRecording by mutableStateOf(false)
    val recordings by mutableStateOf(emptyList<Recording>())

    private val recorder = Recorder()

    fun startRecording(context: Context, fd: FileDescriptor) {
        isRecording = recorder.start(context, fd)
    }

    fun stopRecording() {
        recorder.stop()
        isRecording = false
    }
}
