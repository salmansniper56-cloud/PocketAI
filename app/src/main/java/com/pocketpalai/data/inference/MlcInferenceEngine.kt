package com.pocketpalai.data.inference

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Stub implementation of the MLC (GGUF) inference engine.
 * Currently provides no real inference – it simply logs calls
 * and returns placeholder strings. This satisfies compilation
 * while the real native integration can be added later.
 */
class MlcInferenceEngine private constructor() : InferenceEngine {
    companion object {
        private var INSTANCE: MlcInferenceEngine? = null
        fun getInstance(): MlcInferenceEngine =
            INSTANCE ?: synchronized(this) { INSTANCE ?: MlcInferenceEngine().also { INSTANCE = it } }
    }

    private var loaded = false
    private var modelName: String? = null

    override suspend fun loadModel(filePath: String, modelName: String): Boolean = withContext(Dispatchers.IO) {
        Log.d("MlcInferenceEngine", "loadModel called with $filePath (model=$modelName)")
        this@MlcInferenceEngine.loaded = true
        this@MlcInferenceEngine.modelName = modelName
        true
    }

    override suspend fun unloadModel() = withContext(Dispatchers.IO) {
        Log.d("MlcInferenceEngine", "unloadModel")
        loaded = false
        modelName = null
    }

    override fun isModelLoaded(): Boolean = loaded
    override fun getLoadedModelName(): String? = modelName

    override suspend fun generateStream(
        userPrompt: String,
        systemPrompt: String?,
        chatHistory: String?,
        onToken: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (!loaded) {
            onError(IllegalStateException("Model not loaded"))
            return@withContext
        }
        Log.d("MlcInferenceEngine", "generateStream placeholder")
        val dummy = "[MLC stub response for: $userPrompt]"
        onToken(dummy)
        onComplete(dummy)
    }

    override suspend fun generate(
        userPrompt: String,
        systemPrompt: String?,
        chatHistory: String?
    ): String = withContext(Dispatchers.IO) {
        if (!loaded) return@withContext "Error: model not loaded"
        Log.d("MlcInferenceEngine", "generate placeholder")
        "[MLC stub response for: $userPrompt]"
    }
}
