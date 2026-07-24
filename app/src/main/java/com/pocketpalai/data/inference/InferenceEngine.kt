package com.pocketpalai.data.inference

/**
 * Unified interface for all inference back‑ends.
 */
interface InferenceEngine {
    suspend fun loadModel(filePath: String, modelName: String = "Model"): Boolean
    suspend fun unloadModel()
    fun isModelLoaded(): Boolean
    fun getLoadedModelName(): String?

    suspend fun generateStream(
        userPrompt: String,
        systemPrompt: String? = null,
        chatHistory: String? = null,
        onToken: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun generate(
        userPrompt: String,
        systemPrompt: String? = null,
        chatHistory: String? = null
    ): String
}
