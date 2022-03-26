package me.timpushkin.voicenotes.models

import android.net.Uri

data class Recording(val name: String, val date: Long, val duration: Long, val uri: Uri)
