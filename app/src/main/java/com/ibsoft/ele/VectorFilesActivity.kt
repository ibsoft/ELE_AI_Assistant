package com.ibsoft.ele

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.ibsoft.ele.adapter.VectorFileAdapter
import com.ibsoft.ele.db.AppDatabase
import com.ibsoft.ele.databinding.ActivityVectorFilesBinding
import com.ibsoft.ele.model.VectorFile
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VectorFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVectorFilesBinding
    private lateinit var db: AppDatabase
    private lateinit var vectorFileAdapter: VectorFileAdapter
    private val vectorFileList = mutableListOf<VectorFile>()
    private val activityJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + activityJob)
    private lateinit var api: OpenAIAssistantApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("VectorFilesActivity", "onCreate called")
        binding = ActivityVectorFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set light mode and a title for the action bar
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        supportActionBar?.title = "Vector Files"

        db = AppDatabase.getDatabase(this)

        // Pass a lambda to the adapter that, on click, shows a confirmation dialog
        vectorFileAdapter = VectorFileAdapter(vectorFileList) { vectorFile ->
            showDeleteConfirmationDialog(vectorFile)
        }

        binding.vectorFilesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.vectorFilesRecyclerView.adapter = vectorFileAdapter

        // Setup Retrofit with a logging interceptor
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

        loadVectorFiles()
    }

    /**
     * Loads the list of vector files from the local database.
     */
    private fun loadVectorFiles() {
        uiScope.launch {
            vectorFileList.clear()
            Log.d("VectorFilesActivity", "Loading vector files from database")
            val files = withContext(Dispatchers.IO) { db.vectorFileDao().getAllVectorFiles() }
            vectorFileList.addAll(files)
            vectorFileAdapter.notifyDataSetChanged()
            Log.d("VectorFilesActivity", "Loaded ${files.size} vector files")
            if (files.isEmpty()) {
                Toast.makeText(
                    this@VectorFilesActivity,
                    "No vector files found.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Displays a confirmation dialog before deleting a vector file.
     */
    private fun showDeleteConfirmationDialog(vectorFile: VectorFile) {
        AlertDialog.Builder(this)
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete this file?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                // If user confirms, proceed with delete operation
                uiScope.launch { deleteVectorFile(vectorFile) }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    /**
     * Deletes a vector file from the vector store via API and also from the local database.
     */
    private suspend fun deleteVectorFile(vectorFile: VectorFile) {
        // Check for an active internet connection.
        if (!isNetworkAvailable()) {
            Toast.makeText(
                this@VectorFilesActivity,
                "No internet connection. Please check your connection and try again.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val config = withContext(Dispatchers.IO) { db.apiConfigDao().getConfig() }
        if (config == null) {
            Toast.makeText(
                this@VectorFilesActivity,
                "API configuration is missing. Please update your settings.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val authHeader = "Bearer " + config.openaiApiKey
        try {
            val response = withContext(Dispatchers.IO) {
                api.deleteVectorStoreFile(config.vectorstoreId, authHeader, vectorFile.fileId)
            }
            if (response.isSuccessful) {
                withContext(Dispatchers.IO) {
                    db.vectorFileDao().deleteVectorFile(vectorFile)
                }
                Toast.makeText(
                    this@VectorFilesActivity,
                    "Your file has been successfully deleted.",
                    Toast.LENGTH_SHORT
                ).show()
                loadVectorFiles()
            } else {
                Toast.makeText(
                    this@VectorFilesActivity,
                    "We could not delete your file. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.e("VectorFilesActivity", "Error deleting file: ${e.message}")
            Toast.makeText(
                this@VectorFilesActivity,
                "An unexpected error occurred while deleting your file. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
        }
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
        Log.d("VectorFilesActivity", "onDestroy called")
    }
}
