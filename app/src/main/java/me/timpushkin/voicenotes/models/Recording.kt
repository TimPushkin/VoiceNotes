package me.timpushkin.voicenotes.models

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Recording(
    val name: String,
    val date: Long,
    val duration: Int,
    val uri: Uri
) {
    var played by mutableStateOf(0)

    companion object {
        val EMPTY = Recording("", 0, 0, Uri.EMPTY)
    }
}
