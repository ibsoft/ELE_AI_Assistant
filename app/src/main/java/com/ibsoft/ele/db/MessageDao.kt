package com.ibsoft.ele.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import com.ibsoft.ele.model.Message

@Dao
interface MessageDao {
    @Query("SELECT * FROM Message WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesForConversation(conversationId: Long): List<Message>

    @Insert
    suspend fun insertMessage(message: Message): Long

    @Delete
    suspend fun deleteMessage(message: Message)

    // Annotate deleteAllMessages() with @Query to delete all messages.
    @Query("DELETE FROM Message")
    suspend fun deleteAllMessages(): Int

    @Update
    suspend fun updateMessage(message: Message)

    @Query("SELECT COALESCE(SUM(likes), 0) FROM Message")
    suspend fun getAllMessagesLikes(): Int

    @Query("SELECT COALESCE(SUM(dislikes), 0) FROM Message")
    suspend fun getAllMessagesDislikes(): Int
}
