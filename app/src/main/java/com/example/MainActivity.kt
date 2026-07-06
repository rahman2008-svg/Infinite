package com.example

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.NumberRepository
import com.example.ui.AppContent
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite Room DB
        val database = AppDatabase.getDatabase(this)
        val repository = NumberRepository(
            database.favoriteDao(),
            database.recentDao(),
            database.statsDao()
        )

        // Instantiate state engine
        val viewModel: MainViewModel by viewModels {
            MainViewModelFactory(repository)
        }

        // Initialize Text-To-Speech engine
        tts = TextToSpeech(this, this)

        // Listen for sound request flows from ViewModel
        lifecycleScope.launch {
            viewModel.speakEvent.collect { (text, locale) ->
                if (isTtsInitialized) {
                    tts?.let {
                        it.language = locale
                        it.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
            }
        }

        setContent {
            AppContent(viewModel = viewModel)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            // Pre-warm US English as default fallback voice
            tts?.language = Locale.US
        }
    }

    override fun onDestroy() {
        tts?.let {
            it.stop()
            it.shutdown()
        }
        super.onDestroy()
    }
}
