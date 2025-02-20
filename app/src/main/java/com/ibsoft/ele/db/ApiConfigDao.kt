package com.ibsoft.ele.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ibsoft.ele.model.ApiConfig

@Dao
interface ApiConfigDao {
    @Query("SELECT * FROM api_config WHERE id = 1")
    suspend fun getConfig(): ApiConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(apiConfig: ApiConfig)

    @Update
    suspend fun updateConfig(apiConfig: ApiConfig)
}
