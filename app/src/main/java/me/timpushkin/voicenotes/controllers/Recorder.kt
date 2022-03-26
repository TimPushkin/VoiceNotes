package me.timpushkin.voicenotes.controllers

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.system.Os
import android.util.Log
import java.io.FileDescriptor

private const val TAG = "Recorder"

class Recorder {
    private var mediaRecorder: MediaRecorder? = null
    private var fileDescriptor: FileDescriptor? = null

    fun start(context: Context, output: FileDescriptor): Boolean {
        Log.d(TAG, "Starting recording to $output")

        fileDescriptor = output

        mediaRecorder = getMediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setOutputFile(fileDescriptor)
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

        Log.i(TAG, "Started recording to $fileDescriptor")

        return true
    }

    fun stop() {
        Log.d(TAG, "Stopping recording to $fileDescriptor")

        mediaRecorder?.run {
            try {
                stop()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Called stop() before calling start()")
            }
            release()
        } ?: run {
            Log.e(TAG, "Called stop() when mediaRecorded is uninitialized")
            return
        }

        Os.close(fileDescriptor)
        mediaRecorder = null
        fileDescriptor = null

        Log.i(TAG, "Finished recording")
    }

    private fun getMediaRecorder(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
        else MediaRecorder()
}
