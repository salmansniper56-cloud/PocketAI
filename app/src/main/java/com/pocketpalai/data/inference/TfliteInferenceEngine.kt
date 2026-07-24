package com.pocketpalai.data.inference

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Stub implementation of TensorFlow Lite inference engine.
 * Provides placeholder behavior for compilation.
 */
class TfliteInferenceEngine private constructor() : InferenceEngine {
    companion object {
        private var INSTANCE: TfliteInferenceEngine? = null
        fun getInstance(): TfliteInferenceEngine =
            INSTANCE ?: synchronized(this) { INSTANCE ?: TfliteInferenceEngine().also { INSTANCE = it } }
    }

    private var loaded = false
    private var modelName: String? = null

    override suspend fun loadModel(filePath: String, modelName: String): Boolean = withContext(Dispatchers.IO) {
        Log.d("TfliteInferenceEngine", "loadModel called with $filePath (model=$modelName)")
        this@TfliteInferenceEngine.loaded = true
        this@TfliteInferenceEngine.modelName = modelName
        true
    }

    override suspend fun unloadModel() = withContext(Dispatchers.IO) {
        Log.d("TfliteInferenceEngine", "unloadModel")
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
        val dummy = "[TFLite stub response for: $userPrompt]"
        onToken(dummy)
        onComplete(dummy)
    }

    override suspend fun generate(
        userPrompt: String,
        systemPrompt: String?,
        chatHistory: String?
    ): String = withContext(Dispatchers.IO) {
        if (!loaded) return@withContext "Error: model not loaded"
        "[TFLite stub response for: $userPrompt]"
    }
}
