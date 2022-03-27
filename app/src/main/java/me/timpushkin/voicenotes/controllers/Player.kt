package me.timpushkin.voicenotes.controllers

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.system.Os
import android.util.Log
import java.io.FileDescriptor

private const val TAG = "Player"

class Player {
    private var mediaPlayer: MediaPlayer? = null
    private var fileDescriptor: FileDescriptor? = null

    fun start(input: FileDescriptor, onStarted: () -> Unit = {}) {
        Log.d(TAG, "Starting playing from $input")

        fileDescriptor = input

        val cleanMediaPlayer = mediaPlayer?.apply { reset() } ?: MediaPlayer()
        cleanMediaPlayer.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(fileDescriptor)
            setOnPreparedListener { preparedMediaPlayer ->
                mediaPlayer = preparedMediaPlayer.apply { start() }
                Log.i(TAG, "Started playing from $input")
                onStarted()
            }
            Log.d(TAG, "Preparing to play from $input")
            prepareAsync()
        }
    }

    fun pause() {
        mediaPlayer?.run {
            pause()
            Log.d(TAG, "Paused playing from $fileDescriptor")
        } ?: run { Log.e(TAG, "Failed to pause as MediaPlayer is not set") }
    }

    fun `continue`() {
        mediaPlayer?.run {
            start()
            Log.d(TAG, "Continued playing from $fileDescriptor")
        } ?: run { Log.e(TAG, "Failed to continue as MediaPlayer is not set") }
    }

    fun stop() {
        Log.d(TAG, "Stopping playing from $fileDescriptor")

        mediaPlayer?.run {
            try {
                stop()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Called stop() before calling start()")
            }
            release()
        } ?: run {
            Log.w(TAG, "Called stop() when MediaPlayer is not set")
            return
        }

        Os.close(fileDescriptor)
        mediaPlayer = null
        fileDescriptor = null

        Log.i(TAG, "Finished playing")
    }
}
