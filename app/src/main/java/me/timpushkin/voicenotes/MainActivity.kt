package me.timpushkin.voicenotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import me.timpushkin.voicenotes.ui.MainScreen
import me.timpushkin.voicenotes.ui.theme.VoiceNotesTheme

class MainActivity : ComponentActivity() {
    private lateinit var recordingsViewModel: RecordingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VoiceNotesTheme {
                MainScreen(
                    recordingsViewModel = recordingsViewModel,
                    onPlay = { TODO() },
                    onRecord = { TODO() }
                )
            }
        }
    }
}
