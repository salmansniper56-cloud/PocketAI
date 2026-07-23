package com.pocketpalai.data.db

import androidx.room.*
import com.pocketpalai.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): ChatSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession)

    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(sessionId: String, limit: Int): List<ChatMessage>
}

@Dao
interface PalDao {
    @Query("SELECT * FROM pals")
    fun getAllPals(): Flow<List<Pal>>

    @Query("SELECT * FROM pals WHERE id = :id LIMIT 1")
    suspend fun getPalById(id: String): Pal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPal(pal: Pal)

    @Query("DELETE FROM pals WHERE id = :id")
    suspend fun deletePalById(id: String)
}

@Dao
interface LocalModelDao {
    @Query("SELECT * FROM local_models")
    fun getAllModels(): Flow<List<LocalModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: LocalModel)

    @Query("UPDATE local_models SET isLoaded = 0")
    suspend fun unloadAllModels()

    @Query("UPDATE local_models SET isLoaded = 1 WHERE id = :id")
    suspend fun setModelLoaded(id: String)

    @Query("UPDATE local_models SET isLoaded = 0 WHERE id = :id")
    suspend fun setModelUnloaded(id: String)

    @Query("SELECT * FROM local_models WHERE id = :id LIMIT 1")
    suspend fun getModelById(id: String): LocalModel?

    @Query("DELETE FROM local_models WHERE id = :id")
    suspend fun deleteModelById(id: String)
}

@Dao
interface BenchmarkResultDao {
    @Query("SELECT * FROM benchmark_results ORDER BY date DESC")
    fun getAllResults(): Flow<List<BenchmarkResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: BenchmarkResult)
}
