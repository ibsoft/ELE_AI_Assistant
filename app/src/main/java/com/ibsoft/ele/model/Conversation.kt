package com.ibsoft.ele.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Conversation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    var likes: Int = 0,
    var dislikes: Int = 0
)
