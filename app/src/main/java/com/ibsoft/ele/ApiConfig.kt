package com.ibsoft.ele.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_config")
data class ApiConfig(
    @PrimaryKey val id: Int = 1,
    var openaiApiKey: String,
    var assistantId: String,
    var vectorstoreId: String
)
