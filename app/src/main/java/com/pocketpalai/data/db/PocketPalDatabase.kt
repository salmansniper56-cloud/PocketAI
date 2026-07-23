package com.pocketpalai.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pocketpalai.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChatSession::class,
        ChatMessage::class,
        Pal::class,
        LocalModel::class,
        BenchmarkResult::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PocketPalDatabase : RoomDatabase() {

    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun palDao(): PalDao
    abstract fun localModelDao(): LocalModelDao
    abstract fun benchmarkResultDao(): BenchmarkResultDao

    companion object {
        @Volatile
        private var INSTANCE: PocketPalDatabase? = null

        fun getDatabase(context: Context): PocketPalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PocketPalDatabase::class.java,
                    "pocket_pal_database"
                )
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        instance.seedInitialDataIfNeeded()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }

                instance
            }
        }
    }

    suspend fun seedInitialDataIfNeeded() {
        try {
            if (palDao().getPalById("assistant_default") == null) {
                seedInitialData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun seedInitialData() {
        val palDao = palDao()
        val defaultPals = listOf(
            Pal(
                id = "assistant_default",
                name = "PocketAI",
                type = "Assistant",
                systemPrompt = "You are a helpful, private on-device AI assistant. You give clear, concise, and accurate answers.",
                colorHex = "#10A37F",
                avatar = "🤖",
                role = "General Assistant",
                location = "On-Device",
                isPreset = true
            ),
            Pal(
                id = "pal_coder",
                name = "Code Wizard",
                type = "Assistant",
                systemPrompt = "You are an expert Kotlin, Android, and algorithms developer. Provide clean, efficient code.",
                colorHex = "#00897B",
                avatar = "💻",
                role = "Software Engineer",
                location = "IDE",
                isPreset = true
            ),
            Pal(
                id = "pal_roleplay_detective",
                name = "Inspector Holmes",
                type = "Roleplay",
                systemPrompt = "You are Sherlock Holmes, a brilliant Victorian detective. Analyze facts with sharp logic and dramatic flair.",
                colorHex = "#7B1FA2",
                avatar = "🕵️‍♂️",
                role = "Detective",
                location = "London, 1895",
                isPreset = true
            ),
            Pal(
                id = "pal_cocktail_mixologist",
                name = "Master Mixologist",
                type = "Roleplay",
                systemPrompt = "You are a world-class mixologist crafting gourmet cocktail and mocktail recipes tailored to ingredients on hand.",
                colorHex = "#E65100",
                avatar = "🍸",
                role = "Mixologist",
                location = "Speakeasy Lounge",
                isPreset = true
            )
        )
        defaultPals.forEach { palDao.insertPal(it) }

        val modelDao = localModelDao()
        val defaultModels = listOf(
            LocalModel(
                id = "llama-3.2-1b-instruct",
                name = "Llama 3.2 1B Instruct",
                repo = "bartowski/Llama-3.2-1B-Instruct-GGUF",
                sizeBytes = 780_000_000L,
                quantization = "Q4_K_M",
                isDownloaded = false,
                isLoaded = false,
                contextLength = 8192,
                parameters = "1.0B",
                ramRequiredMb = 900,
                author = "Meta / bartowski",
                description = "Meta Llama 3.2 1B instruction model. Fast, lightweight, and accurate for mobile phones.",
                downloadStatus = "IDLE",
                downloadProgress = 0
            ),
            LocalModel(
                id = "qwen2.5-1.5b-instruct",
                name = "Qwen 2.5 1.5B Instruct",
                repo = "Qwen/Qwen2.5-1.5B-Instruct-GGUF",
                sizeBytes = 1_100_000_000L,
                quantization = "q4_k_m",
                isDownloaded = false,
                isLoaded = false,
                contextLength = 4096,
                parameters = "1.5B",
                ramRequiredMb = 1300,
                author = "Qwen",
                description = "State-of-the-art 1.5B language model with excellent reasoning and coding abilities.",
                downloadStatus = "IDLE",
                downloadProgress = 0
            ),
            LocalModel(
                id = "gemma-2b-it-gguf",
                name = "Gemma 2 2B Instruct",
                repo = "bartowski/Gemma-2-2B-It-GGUF",
                sizeBytes = 1_600_000_000L,
                quantization = "Q4_K_M",
                isDownloaded = false,
                isLoaded = false,
                contextLength = 4096,
                parameters = "2.5B",
                ramRequiredMb = 1800,
                author = "Google / bartowski",
                description = "Google Gemma 2B instruction model. High quality balance for Android devices.",
                downloadStatus = "IDLE",
                downloadProgress = 0
            ),
            LocalModel(
                id = "llama-2-7b-chat",
                name = "Llama 2 7B Chat",
                repo = "TheBloke/Llama-2-7B-Chat-GGUF",
                sizeBytes = 3_800_000_000L,
                quantization = "Q4_K_M",
                isDownloaded = false,
                isLoaded = false,
                contextLength = 4096,
                parameters = "7.0B",
                ramRequiredMb = 4200,
                author = "Meta / TheBloke",
                description = "Meta Llama 2 7B Chat model. High capacity for flagship devices.",
                downloadStatus = "IDLE",
                downloadProgress = 0
            )
        )
        defaultModels.forEach { modelDao.insertModel(it) }
    }
}
