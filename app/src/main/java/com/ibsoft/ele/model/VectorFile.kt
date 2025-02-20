package com.ibsoft.ele.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VectorFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val fileId: String
)
