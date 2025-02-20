package com.ibsoft.ele.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ibsoft.ele.model.Conversation
import com.ibsoft.ele.model.Message
import com.ibsoft.ele.model.ApiConfig
import com.ibsoft.ele.model.VectorFile

@Database(entities = [Conversation::class, Message::class, ApiConfig::class, VectorFile::class], version = 9)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun apiConfigDao(): ApiConfigDao
    abstract fun vectorFileDao(): VectorFileDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chat_assistant_db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
