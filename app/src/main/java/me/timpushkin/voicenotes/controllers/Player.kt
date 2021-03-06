package me.timpushkin.voicenotes.controllers

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.system.ErrnoException
import android.system.Os
import android.util.Log
import java.io.FileDescriptor

private const val TAG = "Player"

class Player {
    private var mediaPlayer: MediaPlayer? = null
    private var fileDescriptor: FileDescriptor? = null

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false
    val position: Int
        get() = mediaPlayer?.currentPosition ?: 0

    fun start(
        input: FileDescriptor,
        position: Int,
        onStarted: () -> Unit = {},
        onCompleted: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        Log.d(TAG, "Starting playing from $input")

        fileDescriptor = input

        val cleanMediaPlayer = mediaPlayer?.apply { reset() } ?: MediaPlayer()
        cleanMediaPlayer.apply {
            setOnErrorListener { _, _, _ ->
                stop()
                onError()
                true
            }
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(fileDescriptor)
            setOnPreparedListener { preparedMediaPlayer ->
                mediaPlayer = preparedMediaPlayer.apply {
                    seekTo(position)
                    start()
                }
                Log.i(TAG, "Started playing from $input")
                onStarted()
            }
            setOnCompletionListener { onCompleted() }
            Log.d(TAG, "Preparing to play from $input")
            prepareAsync()
        }
    }

    fun stop() {
        Log.d(TAG, "Stopping playing from $fileDescriptor")

        mediaPlayer?.run {
            try {
                stop()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to stop MediaPlayer", e)
            }
            release()
        } ?: run {
            Log.w(TAG, "Called stop() when MediaPlayer is not set")
            return
        }

        try {
            Os.close(fileDescriptor)
        } catch (e: ErrnoException) {
            Log.e(TAG, "Failed to close $fileDescriptor", e)
        }

        mediaPlayer = null
        fileDescriptor = null

        Log.i(TAG, "Finished playing")
    }
}
