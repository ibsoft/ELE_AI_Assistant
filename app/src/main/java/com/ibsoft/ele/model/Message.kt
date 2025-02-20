package com.ibsoft.ele.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val sender: String, // "user" or "bot"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    var likes: Int = 0,
    var dislikes: Int = 0,
    var responseTime: Long = 0
)
