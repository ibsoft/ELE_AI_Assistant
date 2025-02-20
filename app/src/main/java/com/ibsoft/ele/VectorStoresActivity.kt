package com.ibsoft.ele

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.ibsoft.ele.adapter.VectorStoreAdapter
import com.ibsoft.ele.db.AppDatabase
import com.ibsoft.ele.databinding.ActivityVectorStoreBinding
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

class VectorStoresActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVectorStoreBinding
    private lateinit var db: AppDatabase
    private lateinit var vectorStoreAdapter: VectorStoreAdapter
    private val vectorStoreList = mutableListOf<VectorStoreResponse>()
    private val activityJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + activityJob)
    private lateinit var api: OpenAIAssistantApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVectorStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        supportActionBar?.title = "Vector Stores"

        db = AppDatabase.getDatabase(this)

        // Setup adapter with delete and copy callbacks.
        vectorStoreAdapter = VectorStoreAdapter(
            vectorStoreList,
            onDeleteClick = { vectorStore ->
                showDeleteConfirmationDialog(vectorStore)
            },
            onCopyClick = { id ->
                copyToClipboard(id)
            }
        )
        binding.vectorStoresRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@VectorStoresActivity)
            adapter = vectorStoreAdapter
        }

        // Setup Retrofit with logging.
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(OpenAIAssistantApi::class.java)

        // Wire up the Refresh button.
        binding.buttonRefresh.setOnClickListener {
            Toast.makeText(this, "Refreshing vector stores...", Toast.LENGTH_SHORT).show()
            loadVectorStores()
        }

        // Wire up the Create New button.
        binding.buttonCreateNewVectorStore.setOnClickListener {
            startActivity(Intent(this, CreateVectorStoreActivity::class.java))
        }

        Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show()
        // Load initial data.
        loadVectorStores()
    }

    /**
     * Displays a confirmation dialog before deleting a vector store.
     */
    private fun showDeleteConfirmationDialog(vectorStore: VectorStoreResponse) {
        AlertDialog.Builder(this)
            .setTitle("Delete Vector Store")
            .setMessage("Are you sure you want to delete this vector store?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Deleting vector store...", Toast.LENGTH_SHORT).show()
                uiScope.launch { deleteVectorStore(vectorStore) }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    /**
     * Helper function to get the API auth header.
     * Returns null if configuration is missing.
     */
    private suspend fun getAuthHeader(): String? {
        val config = withContext(Dispatchers.IO) { db.apiConfigDao().getConfig() }
        return if (config != null && config.openaiApiKey.isNotBlank()) {
            "Bearer ${config.openaiApiKey}"
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@VectorStoresActivity,
                    "API configuration is missing. Please update your settings.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            null
        }
    }

    /**
     * Loads all vector stores from the server.
     */
    private fun loadVectorStores() {
        if (!isNetworkAvailable()) {
            Toast.makeText(
                this@VectorStoresActivity,
                "It seems you're offline. Please check your internet connection and try again.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        uiScope.launch {
            vectorStoreList.clear()
            val authHeader = getAuthHeader() ?: return@launch
            try {
                val response = withContext(Dispatchers.IO) { api.listVectorStores(authHeader) }
                if (response.isSuccessful) {
                    val data = response.body()?.vectorStores ?: emptyList()
                    vectorStoreList.addAll(data)
                    vectorStoreAdapter.notifyDataSetChanged()
                    if (data.isEmpty()) {
                        Toast.makeText(
                            this@VectorStoresActivity,
                            "No vector stores found.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@VectorStoresActivity,
                            "Loaded ${data.size} vector store(s).",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@VectorStoresActivity,
                        "We couldn't load your vector stores at the moment. Please try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("VectorStoresActivity", "Error loading vector stores: ${e.message}")
                Toast.makeText(
                    this@VectorStoresActivity,
                    "Sorry, we had trouble connecting. Please check your internet connection and try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Deletes the specified vector store via the API and refreshes the list.
     */
    private suspend fun deleteVectorStore(vectorStore: VectorStoreResponse) {
        if (!isNetworkAvailable()) {
            Toast.makeText(
                this@VectorStoresActivity,
                "It appears you're offline. Please connect to the internet and try again.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val authHeader = getAuthHeader() ?: return
        try {
            val response = withContext(Dispatchers.IO) { api.deleteVectorStore(vectorStore.id, authHeader) }
            if (response.isSuccessful) {
                Toast.makeText(
                    this,
                    "Your vector store was successfully removed.",
                    Toast.LENGTH_SHORT
                ).show()
                loadVectorStores()
            } else {
                Toast.makeText(
                    this,
                    "We couldn't delete your vector store. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Oops, something went wrong while deleting your vector store. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Copies a given string (vector store ID) to the clipboard.
     */
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Vector Store ID", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Vector store ID copied to clipboard.", Toast.LENGTH_SHORT).show()
    }

    /**
     * Checks whether there is an active internet connection.
     */
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
