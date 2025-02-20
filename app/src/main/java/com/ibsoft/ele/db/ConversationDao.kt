package com.ibsoft.ele.db

import androidx.room.*
import com.ibsoft.ele.model.Conversation

@Dao
interface ConversationDao {
    @Query("SELECT * FROM Conversation ORDER BY timestamp DESC")
    suspend fun getAllConversations(): List<Conversation>

    @Insert
    suspend fun insertConversation(conversation: Conversation): Long

    @Delete
    suspend fun deleteConversation(conversation: Conversation)
}
