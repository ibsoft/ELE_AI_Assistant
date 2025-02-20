package com.ibsoft.ele

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.ibsoft.ele.db.AppDatabase
import kotlinx.coroutines.*
import java.util.Locale

class SplashActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val splashTime: Long = 5000L // 5 seconds
    private val uiScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var tts: TextToSpeech

    // A flag to ensure navigation happens only once.
    private var hasNavigated = false

    companion object {
        // This flag ensures that the welcome voice message is spoken only once per app launch.
        var welcomeMessagePlayed = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Force light mode by default.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initialize TextToSpeech; onInit() will be called when ready.
        tts = TextToSpeech(this, this)

        // Find the progress bar and set its max value.
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBarLoading)
        progressBar.max = 100

        // Start a coroutine to update the progress bar.
        uiScope.launch {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < splashTime) {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = ((elapsed.toFloat() / splashTime) * 100).toInt()
                progressBar.progress = progress
                delay(50) // update every 50ms
            }
            progressBar.progress = 100
        }

        // Launch a coroutine to wait for the splash time and then navigate.
        uiScope.launch {
            val config = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(applicationContext).apiConfigDao().getConfig()
            }
            delay(splashTime)
            // Ensure navigation happens only once.
            if (!hasNavigated) {
                hasNavigated = true
                if (config == null || config.openaiApiKey.isBlank()) {
                    startActivity(Intent(this@SplashActivity, SettingsActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, ConversationListActivity::class.java))
                }
                finish()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US

            // Set voice to female if available
            setFemaleVoice()

            // Check for internet connection
            if (!isInternetAvailable()) {
                val offlineMessage = "Warning! No internet connection detected. Some features may not work properly."
                tts.speak(offlineMessage, TextToSpeech.QUEUE_FLUSH, null, "NoInternetWarning")
                return
            }

            // List of random welcome phrases
            val welcomeMessages = listOf(
                "Hello! My name is ELIE, your AI Assistant.",
                "Hi there! I'm ELIE, how can I assist you today?",
                "Welcome! I'm ELIE, here to help you.",
                "Hey! ELIE at your service.",
                "Greetings! I'm ELIE, your personal assistant.",
                "Nice to meet you! I'm ELIE, let's get started.",
                "Good day! I'm ELIE, your smart assistant. How can I help?",
                "Hi! ELIE here, ready to assist you anytime.",
                "Hello! I'm ELIE. What can I do for you today?",
                "Hey there! Need some help? ELIE is here for you.",
                "Greetings, human! I'm ELIE, your AI companion.",
                "Welcome back! ELIE is ready to make your day easier.",
                "I'm ELIE, your assistant for all things smart and simple.",
                "Hi, I'm ELIE! Let's get things done together.",
                "Hey, it's ELIE! I'm here to support you.",
                "Hello! ELIE checking in. What’s on your mind?",
                "Good to see you! ELIE at your service.",
                "ELIE here! Let’s make things smooth and easy for you.",
                "Hey! Your friendly AI assistant ELIE is ready to help.",
                "Hi there! ELIE is online and ready to assist."
            )

            // Play a random welcome message if not already played
            if (!welcomeMessagePlayed) {
                val randomMessage = welcomeMessages.random()
                tts.speak(randomMessage, TextToSpeech.QUEUE_FLUSH, null, "WelcomeUtterance")
                welcomeMessagePlayed = true
            }
        } else {
            // Log or handle TextToSpeech initialization failure
            Log.e("TTS", "TextToSpeech initialization failed")
        }
    }

    /**
     * Tries to set the TTS voice to a female voice if available.
     */
    private fun setFemaleVoice() {
        val femaleVoices = tts.voices?.filter { it.name.contains("female", ignoreCase = true) }
        val selectedVoice = femaleVoices?.firstOrNull()

        if (selectedVoice != null) {
            tts.voice = selectedVoice
            Log.d("TTS", "Female voice set: ${selectedVoice.name}")
        } else {
            Log.w("TTS", "No female voice found, using default voice")
        }
    }

    /**
     * Checks if the device has an active internet connection.
     */
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivityManager?.let {
            val network = it.activeNetwork ?: return false
            val capabilities = it.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
        uiScope.cancel()
    }
}
