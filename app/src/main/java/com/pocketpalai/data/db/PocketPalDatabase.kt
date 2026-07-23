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
                id = "smollm-360m-instruct",
                name = "SmolLM 360M Ultra-Light",
                repo = "HuggingFaceTB/SmolLM-360M-Instruct-GGUF",
                sizeBytes = 240_000_000L,
                quantization = "Q4_K_M",
                isDownloaded = false,
                isLoaded = false,
                contextLength = 2048,
                parameters = "0.36B",
                ramRequiredMb = 450,
                author = "HuggingFaceTB",
                description = "Ultra lightweight model (240 MB). Best for testing on low-end phones. Download first!",
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
                author = "bartowski",
                description = "Google Gemma 2B instruction model. Good balance of quality and speed for mobile.",
                downloadStatus = "IDLE",
                downloadProgress = 0
            ),
            LocalModel(
                id = "deepseek-r1-qwen-1.5b",
                name = "DeepSeek R1 Distill 1.5B",
                repo = "deepseek-ai/DeepSeek-R1-Distill-Qwen-1.5B-GGUF",
                sizeBytes = 1_100_000_000L,
                quantization = "Q4_K_M",
                isDownloaded = false,
                isLoaded = false,
                contextLength = 8192,
                parameters = "1.5B",
                ramRequiredMb = 1400,
                author = "deepseek-ai",
                description = "Reasoning & chain-of-thought model. Great for problem solving.",
                downloadStatus = "IDLE",
                downloadProgress = 0
            ),
            LocalModel(
                id = "llama-3.2-1b-instruct",
                name = "Llama 3.2 1B Instruct",
                repo = "city96/Llama-3.2-1B-Instruct-GGUF",
                sizeBytes = 850_000_000L,
                quantization = "Q4_K_M",
                isDownloaded = false,
                isLoaded = false,
                contextLength = 8192,
                parameters = "1.0B",
                ramRequiredMb = 950,
                author = "city96",
                description = "Meta Llama 3.2 compact edge model for mobile deployment.",
                downloadStatus = "IDLE",
                downloadProgress = 0
            ),
            LocalModel(
                id = "phi-3.5-mini-instruct",
                name = "Phi-3.5 Mini 3.8B",
                repo = "microsoft/Phi-3.5-mini-instruct-GGUF",
                sizeBytes = 2_300_000_000L,
                quantization = "Q4_K_M",
                isDownloaded = false,
                isLoaded = false,
                contextLength = 4096,
                parameters = "3.8B",
                ramRequiredMb = 2600,
                author = "microsoft",
                description = "Microsoft high-quality model. Best quality but needs more RAM (2.6 GB).",
                downloadStatus = "IDLE",
                downloadProgress = 0
            )
        )
        defaultModels.forEach { modelDao.insertModel(it) }
    }
}
