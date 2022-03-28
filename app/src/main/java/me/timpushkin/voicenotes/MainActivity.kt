package me.timpushkin.voicenotes

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import me.timpushkin.voicenotes.models.Recording
import me.timpushkin.voicenotes.ui.MainScreen
import me.timpushkin.voicenotes.ui.theme.VoiceNotesTheme
import me.timpushkin.voicenotes.utils.StorageHandler

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var applicationState: ApplicationState
    private lateinit var storageHandler: StorageHandler

    private val permissionsRequester =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Log.d(TAG, "Permissions request result: $permissions")

            val recordGranted = permissions.getOrElse(Manifest.permission.RECORD_AUDIO) {
                checkPermission(Manifest.permission.RECORD_AUDIO) == PermissionStatus.GRANTED
            }
            val writeGranted = permissions.getOrElse(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Build.VERSION_CODES.P
                ) == PermissionStatus.GRANTED
            }
            if (recordGranted && writeGranted) startRecording()
            else applicationState.showSnackbar(resources.getString(R.string.no_permissions))
        }

    private var audioService: AudioService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            if (binder is AudioService.AudioServiceBinder) {
                Log.d(TAG, "Connected to audio service")
                audioService = binder.service
            } else Log.e(TAG, "Connected to an unknown service $name")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            if (name.className == AudioService::class.qualifiedName) {
                Log.w(TAG, "Lost connection to audio service")
            } else Log.e(TAG, "Lost connection to an unknown service $name")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageHandler = StorageHandler(
            contentResolver,
            packageManager.getApplicationLabel(applicationInfo).toString()
        )
        applicationState = ApplicationState().apply {
            setRecordingsWith(storageHandler::getRecordings)
        }

        Log.d(TAG, "Binding to audio service")

        val service = Intent(this, AudioService::class.java)
        startService(service)
        bindService(service, serviceConnection, Context.BIND_IMPORTANT)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContent {
            VoiceNotesTheme {
                MainScreen(
                    applicationState = applicationState,
                    onPlay = this::startPlaying,
                    onPause = this::stopPlaying,
                    onStartRecording = this::startRecording,
                    onStopRecording = this::stopRecording
                )
            }
        }
    }

    private fun startPlaying(recording: Recording) {
        if (applicationState.isRecording) audioService?.run {
            stopRecording()
            applicationState.isRecording = false
        } ?: run {
            Log.e(TAG, "Cannot stop recording in progress: audio service unavailable")
            return
        }

        audioService?.startPlaying(
            uri = recording.uri,
            position = recording.played,
            onPlayerStarted = { applicationState.nowPlaying = recording },
            onPlayerProgress = { applicationState.nowPlaying.played = it },
            onPlayerCompleted = {
                applicationState.nowPlaying.played = 0
                applicationState.nowPlaying = Recording.EMPTY
            },
            onError = {
                applicationState.nowPlaying = Recording.EMPTY
                applicationState.showSnackbar(resources.getString(R.string.play_failed))
            }
        ) ?: Log.e(TAG, "Cannot start playing: audio service unavailable")
    }

    private fun stopPlaying() {
        audioService?.run {
            stopPlaying()
            applicationState.nowPlaying = Recording.EMPTY
        } ?: Log.e(TAG, "Cannot stop playing: audio service unavailable")
    }

    private fun startRecording() {
        Log.d(TAG, "Attempting to start recording")

        val recordPermission = checkPermission(Manifest.permission.RECORD_AUDIO)
        val writePermission = checkPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Build.VERSION_CODES.P
        )

        if (recordPermission == PermissionStatus.GRANTED && writePermission == PermissionStatus.GRANTED) {
            Log.d(TAG, "All permissions are granted, starting recording")

            if (applicationState.nowPlaying != Recording.EMPTY) stopPlaying()

            storageHandler.createRecording()?.let { uri ->
                val started = audioService?.startRecording(uri) ?: run {
                    Log.e(TAG, "Cannot start recording: audio service unavailable")
                    false
                }
                if (started) applicationState.startRecordingTimer()
                else applicationState.showSnackbar(resources.getString(R.string.record_failed))
                applicationState.isRecording = started
            }

            return
        }

        Log.d(TAG, "Lacking some permissions to start recording")

        val permissionsToRationalize = mutableListOf<String>()
        val rationales = mutableListOf<String>()
        if (recordPermission == PermissionStatus.RATIONALIZE) {
            permissionsToRationalize.add(Manifest.permission.RECORD_AUDIO)
            rationales.add(resources.getString(R.string.audio_recording_rationale))
        }
        if (writePermission == PermissionStatus.RATIONALIZE) {
            permissionsToRationalize.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            rationales.add(resources.getString(R.string.storage_writing_rationale))
        }
        if (permissionsToRationalize.isNotEmpty()) {
            Log.d(TAG, "Showing rationales for $permissionsToRationalize")
            applicationState.showSnackbar(
                message = rationales.joinToString("\n"),
                label = resources.getString(R.string.grant),
                action = { permissionsRequester.launch(permissionsToRationalize.toTypedArray()) }
            )
        }

        val permissionsToRequest = mutableListOf<String>().apply {
            if (recordPermission == PermissionStatus.REQUEST) add(Manifest.permission.RECORD_AUDIO)
            if (writePermission == PermissionStatus.REQUEST) add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            Log.d(TAG, "Requesting permissions $permissionsToRequest")
            permissionsRequester.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun stopRecording() {
        if (applicationState.isRecording) {
            audioService?.run {
                stopRecording()
                applicationState.apply {
                    isRecording = false
                    stopRecordingTimer()
                    setRecordingsWith(storageHandler::getRecordings)
                }
            } ?: Log.e(TAG, "Cannot stop recording: audio service unavailable")
        } else Log.e(TAG, "Attempted to stop recording when nothing is recording")
    }

    private fun checkPermission(permission: String, maxVersion: Int = Int.MAX_VALUE) = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Build.VERSION.SDK_INT > maxVersion ||
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
        shouldShowRequestPermissionRationale(permission) -> PermissionStatus.RATIONALIZE
        else -> PermissionStatus.REQUEST
    }

    private enum class PermissionStatus { GRANTED, REQUEST, RATIONALIZE }
}
