package me.timpushkin.voicenotes.controllers

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.system.ErrnoException
import android.system.Os
import android.util.Log
import java.io.FileDescriptor

private const val TAG = "Recorder"

class Recorder {
    private var mediaRecorder: MediaRecorder? = null
    private var fileDescriptor: FileDescriptor? = null

    fun start(context: Context, output: FileDescriptor): Boolean {
        Log.d(TAG, "Starting recording to $output")

        if (mediaRecorder != null) {
            Log.w(TAG, "Previous recording was not stopped. Stopping...")
            stop()
        }

        fileDescriptor = output

        mediaRecorder = getMediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setOutputFile(fileDescriptor)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                prepare()
            } catch (e: java.io.IOException) {
                Log.e(TAG, "Failed to prepare MediaRecorder")
                release()
                return false
            }
            start()
        }

        Log.i(TAG, "Started recording to $fileDescriptor")

        return true
    }

    fun stop(): Boolean {
        Log.d(TAG, "Stopping recording to $fileDescriptor")

        mediaRecorder?.run {
            try {
                stop()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to stop MediaRecorder", e)
            }
            release()
        } ?: run {
            Log.w(TAG, "Called stop() when MediaRecorded is not set")
            return false
        }

        try {
            Os.close(fileDescriptor)
        } catch (e: ErrnoException) {
            Log.e(TAG, "Failed to close $fileDescriptor", e)
        }

        mediaRecorder = null
        fileDescriptor = null

        Log.i(TAG, "Finished recording")

        return true
    }

    private fun getMediaRecorder(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
        else MediaRecorder()
}
