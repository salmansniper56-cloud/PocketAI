package com.pocketpalai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "chat_sessions")
@Serializable
data class ChatSession(
    @PrimaryKey val id: String,
    val title: String,
    val palId: String = "assistant_default",
    val modelId: String = "gemma-2b-it-gguf",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
@Serializable
data class ChatMessage(
    @PrimaryKey val id: String,
    val sessionId: String,
    val sender: String, // "user", "assistant", or "system"
    val text: String,
    val reasoning: String? = null,
    val toolCall: String? = null,
    val toolOutput: String? = null,
    val htmlPreview: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isPending: Boolean = false,
    val thinkingTimeMs: Long = 0
)

@Entity(tableName = "pals")
@Serializable
data class Pal(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "Assistant" or "Roleplay"
    val systemPrompt: String,
    val colorHex: String = "#1976D2",
    val avatar: String = "🤖",
    val role: String = "AI Assistant",
    val location: String = "Global",
    val enableTools: Boolean = true,
    val isPreset: Boolean = false
)

@Entity(tableName = "local_models")
@Serializable
data class LocalModel(
    @PrimaryKey val id: String,
    val name: String,
    val repo: String,
    val sizeBytes: Long,
    val quantization: String, // "Q4_K_M", "Q8_0", "F16"
    val isDownloaded: Boolean = false,
    val isLoaded: Boolean = false,
    val contextLength: Int = 4096,
    val parameters: String = "2.7B",
    val filePath: String? = null,
    val downloadProgress: Int = 0,
    val downloadStatus: String = "IDLE", // "IDLE", "DOWNLOADING", "PAUSED", "COMPLETED", "ERROR"
    val downloadedBytes: Long = 0L,
    val ramRequiredMb: Int = 2048,
    val author: String = "HuggingFace",
    val description: String = "GGUF Quantized Large Language Model"
)

@Entity(tableName = "benchmark_results")
@Serializable
data class BenchmarkResult(
    @PrimaryKey val id: String,
    val modelName: String,
    val tokensPerSec: Double,
    val ttftMs: Long,
    val ramUsageMb: Int,
    val date: Long = System.currentTimeMillis()
)

data class HuggingFaceModel(
    val id: String,
    val name: String,
    val author: String,
    val downloads: Int,
    val likes: Int,
    val quantizations: List<String>,
    val sizeBytes: Long = 2_100_000_000L,
    val ramRequiredMb: Int = 2500,
    val isGated: Boolean = false,
    val description: String = "",
    val downloadUrl: String = "",
    val parameters: String = "3B",
    val tags: List<String> = emptyList()
)

data class TtsEngine(
    val id: String,
    val name: String,
    val description: String,
    val isDownloaded: Boolean,
    val voiceName: String
)
