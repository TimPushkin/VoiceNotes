package me.timpushkin.voicenotes.controllers

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.FileDescriptor

private const val TAG = "Recorder"

class Recorder(private val context: Context) {
    private lateinit var mediaRecorder: MediaRecorder

    fun start(fd: FileDescriptor): Boolean {
        mediaRecorder = getMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setOutputFile(fd)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                prepare()
            } catch (e: java.io.IOException) {
                Log.e(TAG, "Failed to prepare a mediaRecorder")
                release()
                return false
            }
            start()
        }

        Log.i(TAG, "Started recording to $fd")

        return true
    }

    fun stop() {
        if (!this::mediaRecorder.isInitialized) {
            Log.e(TAG, "Called stop() when mediaRecorded is uninitialized")
            return
        }

        mediaRecorder.run {
            try {
                stop()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Called stop() before calling start()")
            }
            release()
        }

        Log.i(TAG, "Finished recording")
    }

    private fun getMediaRecorder() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
        else MediaRecorder()
}
