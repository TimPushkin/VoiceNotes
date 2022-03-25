package me.timpushkin.voicenotes.models

data class Recording(
    val id: Long,
    val title: String,
    val timestamp: Long,
    val duration: Long
)
