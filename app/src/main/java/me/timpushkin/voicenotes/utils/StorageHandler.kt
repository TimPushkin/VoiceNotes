package me.timpushkin.voicenotes.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.FileDescriptor
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "StorageHandler"

private const val FILE_EXTENSION = ".aac"

class StorageHandler(private val resolver: ContentResolver) {
    private val audioCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.ENGLISH)
    private val calendar = Calendar.getInstance()

    fun createAudioFile(): FileDescriptor? {
        val fileDetails = ContentValues().apply {
            put(
                MediaStore.Audio.Media.DISPLAY_NAME,
                formatter.format(calendar.time) + FILE_EXTENSION
            )
        }

        val uri = resolver.insert(audioCollection, fileDetails)
        if (uri == null) {
            Log.e(TAG, "File creation failed")
            return null
        }

        val fd = resolver.openFileDescriptor(uri, "w")
        if (fd == null) {
            Log.e(TAG, "Failed to obtain a file descriptor for the created file")
            return null
        }

        return fd.fileDescriptor
    }
}
