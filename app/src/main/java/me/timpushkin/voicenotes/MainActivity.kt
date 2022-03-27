package me.timpushkin.voicenotes

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import me.timpushkin.voicenotes.ui.MainScreen
import me.timpushkin.voicenotes.ui.theme.VoiceNotesTheme
import me.timpushkin.voicenotes.utils.StorageHandler

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var applicationState: ApplicationState
    private lateinit var storageHandler: StorageHandler

    private var currentRecording: Uri? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageHandler = StorageHandler(
            contentResolver,
            packageManager.getApplicationLabel(applicationInfo).toString()
        )
        applicationState = ApplicationState().apply {
            setRecordingsWith(storageHandler::getRecordings)
        }

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

    private fun startPlaying(recording: Uri) {
        storageHandler.uriToFileDescriptor(recording, StorageHandler.Mode.READ)?.let { fd ->
            applicationState.startPlaying(recording, fd)
        }
    }

    private fun stopPlaying(recording: Uri) {
        applicationState.stopPlaying()
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

            storageHandler.createRecording()?.let { uri ->
                currentRecording = uri
                storageHandler.uriToFileDescriptor(uri, StorageHandler.Mode.WRITE)?.let { fd ->
                    applicationState.startRecording(this, fd)
                }
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
        currentRecording?.let { uri ->
            Log.d(TAG, "Stopping recording")
            applicationState.stopRecording()
            storageHandler.setMetadataFor(uri)
            applicationState.setRecordingsWith(storageHandler::getRecordings)
        } ?: run { Log.e(TAG, "Attempted to stop recording when current recording isn't set") }
    }

    private fun checkPermission(permission: String, maxVersion: Int = Int.MAX_VALUE) = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Build.VERSION.SDK_INT > maxVersion ||
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
        shouldShowRequestPermissionRationale(permission) -> PermissionStatus.RATIONALIZE
        else -> PermissionStatus.REQUEST
    }

    private enum class PermissionStatus { GRANTED, REQUEST, RATIONALIZE }
}
