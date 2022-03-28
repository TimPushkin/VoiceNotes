package me.timpushkin.voicenotes.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import me.timpushkin.voicenotes.models.Recording
import java.io.File
import java.io.FileDescriptor
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "StorageHandler"

private const val FILE_EXTENSION = ".aac"

class StorageHandler(private val resolver: ContentResolver, subfolder: String) {
    private val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
    private val calendar = Calendar.getInstance()

    private val collection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val dir = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> Environment.DIRECTORY_RECORDINGS
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> Environment.DIRECTORY_PODCASTS
        else -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).absolutePath
    } + if (subfolder.isNotBlank()) "${File.separator}$subfolder" else ""

    fun createRecording(): Uri? {
        val fileDetails = ContentValues().apply {
            val name = formatter.format(calendar.time) + FILE_EXTENSION
            put(MediaStore.Audio.Media.DISPLAY_NAME, name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                put(MediaStore.Audio.Media.RELATIVE_PATH, dir)
            else put(MediaStore.Audio.Media.DATA, "$dir${File.separator}$name")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val uri = resolver.insert(collection, fileDetails)
        if (uri == null) Log.e(TAG, "Recording file creation failed")
        else Log.d(TAG, "Created recording $uri")
        return uri
    }

    fun uriToFileDescriptor(uri: Uri, mode: Mode): FileDescriptor? {
        val modeStr = when (mode) {
            Mode.READ -> "r"
            Mode.WRITE -> "w"
        }
        val fd = resolver.openFileDescriptor(uri, modeStr)
        if (fd == null) Log.e(TAG, "Failed to obtain a file descriptor for the created file")
        else Log.d(TAG, "Created file descriptor $fd for Uri $uri")
        return fd?.fileDescriptor
    }

    enum class Mode { READ, WRITE }

    fun setMetadataFor(uri: Uri) {
        val metadata = ContentValues().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                put(MediaStore.Audio.Media.IS_PENDING, 0)
        }
        if (resolver.update(uri, metadata, null, null) == 1)
            Log.d(TAG, "Metadata successfully set for $uri")
        else Log.e(TAG, "Failed to set metadata for $uri")
    }

    fun deleteRecording(uri: Uri) {
        if (resolver.delete(uri, null, null) == 1)
            Log.d(TAG, "Successfully deleted $uri")
        else Log.e(TAG, "Failed to delete $uri")
    }

    fun renameRecording(uri: Uri, name: String) {
        val fileDetails = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, name + FILE_EXTENSION)
        }
        if (resolver.update(uri, fileDetails, null, null) == 1)
            Log.d(TAG, "Successfully renamed $uri to $name")
        else Log.e(TAG, "Failed to rename $uri to $name")
    }

    fun getRecordings(): List<Recording> {
        val recordings = mutableListOf<Recording>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DURATION
        )

        val selection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
            else "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("$dir%")

        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        resolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val date = cursor.getLong(dateColumn) * 1000
                val duration = cursor.getInt(durationColumn)

                val uri = ContentUris.withAppendedId(collection, id)

                recordings += Recording(name.substringBeforeLast('.'), date, duration, uri)
            }
        }

        return recordings
    }
}
