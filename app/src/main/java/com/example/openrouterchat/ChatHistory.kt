package com.example.openrouterchat

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

@Entity
data class ChatHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val model: String,
    @TypeConverters(Converters::class)
    val messages: List<Message>,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM chathistory ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ChatHistory>>

    @Query("SELECT * FROM chathistory WHERE model = :model")
    fun getByModel(model: String): Flow<List<ChatHistory>>

    @Insert
    suspend fun insert(chatHistory: ChatHistory)

    @Delete
    suspend fun delete(chatHistory: ChatHistory)

    @Query("DELETE FROM chathistory WHERE timestamp < :cutoff")
    suspend fun deleteOldChats(cutoff: Long)

    @Query("DELETE FROM chathistory WHERE model = :model")
    suspend fun deleteByModel(model: String)
}

@Database(entities = [ChatHistory::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatHistoryDao(): ChatHistoryDao
}

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromMessageList(messages: List<Message>): String {
        return gson.toJson(messages)
    }

    @TypeConverter
    fun toMessageList(json: String): List<Message> {
        val type = object : TypeToken<List<Message>>() {}.type
        return gson.fromJson(json, type)
    }
}