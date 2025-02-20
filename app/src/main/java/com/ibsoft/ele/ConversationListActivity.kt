package com.ibsoft.ele

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.ibsoft.ele.adapter.ConversationAdapter
import com.ibsoft.ele.db.AppDatabase
import com.ibsoft.ele.model.Conversation
import com.ibsoft.ele.databinding.ActivityConversationListBinding
import kotlinx.coroutines.*

class ConversationListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversationListBinding
    private lateinit var conversationAdapter: ConversationAdapter
    private var conversationList = mutableListOf<Conversation>()
    private lateinit var db: AppDatabase
    private val activityJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + activityJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Set up the toolbar as the support ActionBar
        setSupportActionBar(binding.toolbar)
        // Replace the default overflow icon (3 dots) with a custom tools icon if desired
        binding.toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_tools)

        db = AppDatabase.getDatabase(this)

        conversationAdapter = ConversationAdapter(conversationList,
            onItemClick = { conversation ->
                if (isNetworkAvailable()) {
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("conversationId", conversation.id)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "No internet connection.", Toast.LENGTH_LONG).show()
                }
            },
            onItemLongClick = { conversation ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Conversation")
                    .setMessage("Are you sure you want to delete this conversation?")
                    .setPositiveButton("Yes") { _, _ ->
                        uiScope.launch {
                            withContext(Dispatchers.IO) {
                                db.conversationDao().deleteConversation(conversation)
                            }
                            loadConversations()
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )

        binding.conversationRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.conversationRecyclerView.adapter = conversationAdapter

        // Attach ItemTouchHelper to enable swipe right to delete with confirmation
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // We are not supporting move action
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val conversation = conversationList[position]

                // Show confirmation dialog
                AlertDialog.Builder(this@ConversationListActivity)
                    .setTitle("Delete Conversation")
                    .setMessage("Are you sure you want to delete this conversation?")
                    .setPositiveButton("Yes") { _, _ ->
                        uiScope.launch {
                            withContext(Dispatchers.IO) {
                                db.conversationDao().deleteConversation(conversation)
                            }
                            conversationList.removeAt(position)
                            conversationAdapter.notifyItemRemoved(position)
                        }
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        // Restore the swiped item
                        conversationAdapter.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.conversationRecyclerView)

        binding.fabAddConversation.setOnClickListener {
            showCreateConversationDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadConversations()
    }

    private fun loadConversations() {
        uiScope.launch {
            conversationList.clear()
            val convos = withContext(Dispatchers.IO) {
                db.conversationDao().getAllConversations()
            }
            conversationList.addAll(convos)
            conversationAdapter.notifyDataSetChanged()

            // Calculate aggregated likes and dislikes from the Message table
            val totalLikes = withContext(Dispatchers.IO) { db.messageDao().getAllMessagesLikes() }
            val totalDislikes = withContext(Dispatchers.IO) { db.messageDao().getAllMessagesDislikes() }
            binding.textLikesDislikes.text = "Likes: $totalLikes  Dislikes: $totalDislikes"
        }
    }

    private fun showCreateConversationDialog() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("New Conversation")
            .setMessage("Enter conversation title:")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val title = input.text.toString()
                if (title.isNotEmpty()) {
                    uiScope.launch {
                        val newId = withContext(Dispatchers.IO) {
                            db.conversationDao().insertConversation(Conversation(title = title))
                        }
                        loadConversations()
                        // Check if internet is available before redirecting
                        if (isNetworkAvailable()) {
                            val intent = Intent(this@ConversationListActivity, ChatActivity::class.java)
                            intent.putExtra("conversationId", newId)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@ConversationListActivity, "No internet connection.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_vector_files -> {  // <-- Added case
                startActivity(Intent(this, VectorFilesActivity::class.java))
                true
            }
            R.id.action_assistants -> {  // <-- Added case
                startActivity(Intent(this, AssistantsActivity::class.java))
                true
            }
            R.id.action_vector_stores -> {  // <-- Added case
                startActivity(Intent(this, VectorStoresActivity::class.java))
                true
            }
            R.id.action_about -> {  // <-- Added case
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityJob.cancel()
        uiScope.cancel()
    }
}
