package com.ibsoft.ele

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.ibsoft.ele.databinding.ActivityCreateAssistantBinding
import com.ibsoft.ele.db.AppDatabase
import com.ibsoft.ele.model.AssistantResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CreateAssistantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAssistantBinding
    private lateinit var db: AppDatabase
    private val activityJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + activityJob)
    private lateinit var api: OpenAIAssistantApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Force light mode and set a title for the action bar.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        supportActionBar?.title = "Create Assistant"

        // Initialize the database.
        db = AppDatabase.getDatabase(this)

        // Setup the spinner for selecting a tool.
        // Make sure you have defined a string-array "assistant_tools" in res/values/strings.xml.
        val toolsArray = resources.getStringArray(R.array.assistant_tools)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, toolsArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTools.adapter = adapter

        // Setup Retrofit with logging interceptor.
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(OpenAIAssistantApi::class.java)

        // Set click listener on the Create Assistant button.
        binding.buttonCreateAssistant.setOnClickListener {
            createAssistant()
        }
    }

    private fun createAssistant() {
        // Retrieve field values.
        val instructions = binding.editTextInstructions.text.toString().trim()
        val assistantName = binding.editTextAssistantName.text.toString().trim()
        val model = binding.editTextModel.text.toString().trim()
        val selectedTool = binding.spinnerTools.selectedItem.toString()

        if (instructions.isEmpty() || assistantName.isEmpty() || model.isEmpty() || selectedTool.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields to create your assistant.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the request object.
        val request = CreateAssistantRequest(
            instructions = instructions,
            name = assistantName,
            tools = listOf(Tool(type = selectedTool)),
            model = model
        )

        uiScope.launch {
            // Check for an active internet connection.
            if (!isNetworkAvailable()) {
                Toast.makeText(
                    this@CreateAssistantActivity,
                    "It looks like you're offline. Please check your internet connection and try again.",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            // Retrieve API configuration from the database.
            val config = withContext(Dispatchers.IO) { db.apiConfigDao().getConfig() }
            if (config == null) {
                Toast.makeText(
                    this@CreateAssistantActivity,
                    "It seems your API configuration is missing. Please update it in Settings.",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }
            val authHeader = "Bearer " + config.openaiApiKey
            try {
                val response = withContext(Dispatchers.IO) { api.createAssistant(authHeader, request) }
                if (response.isSuccessful) {
                    val assistant: AssistantResponse? = response.body()
                    Toast.makeText(
                        this@CreateAssistantActivity,
                        "Your assistant was created successfully! ID: ${assistant?.id}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish() // Close the activity after successful creation.
                } else {
                    Toast.makeText(
                        this@CreateAssistantActivity,
                        "We couldn't create your assistant at the moment. Please try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CreateAssistantActivity,
                    "Oops, something went wrong while creating your assistant. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    override fun onDestroy() {
        super.onDestroy()
        activityJob.cancel()
    }
}
