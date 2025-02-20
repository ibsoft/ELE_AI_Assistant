package com.ibsoft.ele.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ibsoft.ele.model.VectorFile

@Dao
interface VectorFileDao {
    @Query("SELECT * FROM VectorFile")
    suspend fun getAllVectorFiles(): List<VectorFile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVectorFile(vectorFile: VectorFile)

    @Delete
    suspend fun deleteVectorFile(vectorFile: VectorFile)
}
