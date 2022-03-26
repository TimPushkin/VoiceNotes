package me.timpushkin.voicenotes.models

interface SnackbarContent {
    val message: String
    val label: String?
    val action: () -> Unit

    operator fun component1() = message
    operator fun component2() = label
    operator fun component3() = action
}
