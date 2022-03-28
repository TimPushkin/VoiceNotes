package me.timpushkin.voicenotes

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.timpushkin.voicenotes.controllers.Player
import me.timpushkin.voicenotes.controllers.Recorder
import me.timpushkin.voicenotes.utils.StorageHandler

private const val TAG = "AudioService"

private const val FOREGROUND_ID = 1
private const val NOTIFICATION_CHANNEL = "audio_service"

class AudioService : Service() {
    private val binder = AudioServiceBinder()
    private var scope = CoroutineScope(Dispatchers.Default)

    private lateinit var storageHandler: StorageHandler
    private lateinit var player: Player
    private lateinit var recorder: Recorder

    private var isNotificationChannelCreated = false
    private var currentRecording: Uri? = null

    inner class AudioServiceBinder : Binder() {
        val service = this@AudioService
    }

    override fun onCreate() {
        storageHandler = StorageHandler(
            contentResolver,
            packageManager.getApplicationLabel(applicationInfo).toString()
        )
        Log.i(TAG, "Was created successfully")
    }

    fun startPlaying(
        recording: Uri,
        onPlayerStarted: () -> Unit = {},
        onPlayerProgress: (Int) -> Unit = {},
        onPlayerCompleted: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        Log.d(TAG, "Received request to start playing $recording")

        storageHandler.uriToFileDescriptor(recording, StorageHandler.Mode.READ)?.let { fd ->
            if (!this::player.isInitialized) player = Player()

            player.start(
                input = fd,
                onStarted = {
                    startForeground(FOREGROUND_ID, buildNotification())
                    onPlayerStarted()
                    scope.launch {
                        while (player.isPlaying) {
                            onPlayerProgress(player.position)
                            delay(500)
                        }
                    }
                },
                onCompleted = {
                    stopForeground(true)
                    onPlayerCompleted()
                },
                onError = {
                    stopForeground(true)
                    onError()
                }
            )
        } ?: run {
            Log.e(TAG, "Failed to get a file descriptor from Uri")
            onError()
        }
    }

    fun pausePlaying() {
        Log.d(TAG, "Received request to pause playing")

        if (this::player.isInitialized) {
            player.pause()
            stopForeground(true)
        } else Log.e(TAG, "Cannot pause: player not set")
    }

    fun continuePlaying() {
        Log.d(TAG, "Received request to continue playing")

        if (this::player.isInitialized) {
            startForeground(FOREGROUND_ID, buildNotification())
            player.`continue`()
        } else Log.e(TAG, "Cannot continue: player not set")
    }

    fun stopPlaying() {
        Log.d(TAG, "Received request to stop playing")

        if (this::player.isInitialized) {
            player.stop()
            stopForeground(true)
        } else Log.e(TAG, "Cannot stop: player not set")
    }

    fun startRecording(recording: Uri): Boolean {
        Log.d(TAG, "Received request to start recording")

        storageHandler.uriToFileDescriptor(recording, StorageHandler.Mode.WRITE)?.let { fd ->
            if (!this::recorder.isInitialized) recorder = Recorder()

            return recorder.start(this, fd).also { started ->
                if (started) currentRecording = recording
            }
        } ?: Log.e(TAG, "Failed to get a file descriptor from Uri")

        return false
    }

    fun stopRecording() {
        Log.d(TAG, "Received request to stop recording")

        val recording = currentRecording

        when {
            !this::recorder.isInitialized -> Log.e(TAG, "Cannot stop: recorder not set")
            recording == null -> Log.e(TAG, "Nothing is recording")
            recorder.stop() -> storageHandler.setMetadataFor(recording)
            else -> storageHandler.deleteRecording(recording)
        }

        currentRecording = null
    }

    override fun onBind(intent: Intent?) = binder

    override fun onDestroy() {
        if (this::player.isInitialized && player.isPlaying) stopPlaying()
        if (this::recorder.isInitialized && currentRecording != null) stopRecording()
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isNotificationChannelCreated) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                resources.getString(R.string.audio_service_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = resources.getString(R.string.audio_service_description) }
            val notificationService =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationService.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setContentTitle(resources.getString(R.string.recording_playing))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
