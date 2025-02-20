package com.ibsoft.ele

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.ibsoft.ele.databinding.ActivityCreateVectorStoreBinding
import com.ibsoft.ele.db.AppDatabase
import com.ibsoft.ele.model.CreateVectorStoreRequest
import com.ibsoft.ele.model.VectorStoreResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CreateVectorStoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateVectorStoreBinding
    private lateinit var db: AppDatabase
    private val activityJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + activityJob)
    private lateinit var api: OpenAIAssistantApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateVectorStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Force light mode and set action bar title.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        supportActionBar?.title = "Create Vector Store"

        // Initialize the Room database.
        db = AppDatabase.getDatabase(this)

        // Setup Retrofit with a logging interceptor.
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

        binding.buttonCreateVectorStore.setOnClickListener {
            createVectorStore()
        }
    }

    private fun createVectorStore() {
        // Retrieve the vector store name.
        val storeName = binding.editTextStoreName.text.toString().trim()
        if (storeName.isEmpty()) {
            Toast.makeText(this, "Please enter a vector store name", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the request object.
        val request = CreateVectorStoreRequest(name = storeName)

        uiScope.launch {
            // Retrieve API configuration from the database.
            val config = withContext(Dispatchers.IO) { db.apiConfigDao().getConfig() }
            if (config == null) {
                Toast.makeText(
                    this@CreateVectorStoreActivity,
                    "API configuration not set. Please configure it in Settings.",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            // Check for an active internet connection.
            if (!isNetworkAvailable()) {
                Toast.makeText(
                    this@CreateVectorStoreActivity,
                    "No internet connection. Please check your connection.",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            val authHeader = "Bearer " + config.openaiApiKey
            try {
                val response = withContext(Dispatchers.IO) {
                    api.createVectorStore(authHeader, request)
                }
                if (response.isSuccessful) {
                    val vectorStore: VectorStoreResponse? = response.body()
                    Toast.makeText(
                        this@CreateVectorStoreActivity,
                        "Vector Store created with ID: ${vectorStore?.id}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish() // Close the activity after successful creation.
                } else {
                    Toast.makeText(
                        this@CreateVectorStoreActivity,
                        "Failed to create vector store. Please try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CreateVectorStoreActivity,
                    "An error occurred while creating the vector store. Please try again later.",
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
