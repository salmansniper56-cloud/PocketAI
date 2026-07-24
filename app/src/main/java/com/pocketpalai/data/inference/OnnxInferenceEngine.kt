package com.pocketpalai.data.inference

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Stub implementation of the ONNX Runtime inference engine.
 * Provides placeholder behavior to satisfy compilation.
 */
class OnnxInferenceEngine private constructor() : InferenceEngine {
    companion object {
        private var INSTANCE: OnnxInferenceEngine? = null
        fun getInstance(): OnnxInferenceEngine =
            INSTANCE ?: synchronized(this) { INSTANCE ?: OnnxInferenceEngine().also { INSTANCE = it } }
    }

    private var loaded = false
    private var modelName: String? = null

    override suspend fun loadModel(filePath: String, modelName: String): Boolean = withContext(Dispatchers.IO) {
        Log.d("OnnxInferenceEngine", "loadModel called with $filePath (model=$modelName)")
        this@OnnxInferenceEngine.loaded = true
        this@OnnxInferenceEngine.modelName = modelName
        true
    }

    override suspend fun unloadModel() = withContext(Dispatchers.IO) {
        Log.d("OnnxInferenceEngine", "unloadModel")
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
        val dummy = "[ONNX stub response for: $userPrompt]"
        onToken(dummy)
        onComplete(dummy)
    }

    override suspend fun generate(
        userPrompt: String,
        systemPrompt: String?,
        chatHistory: String?
    ): String = withContext(Dispatchers.IO) {
        if (!loaded) return@withContext "Error: model not loaded"
        "[ONNX stub response for: $userPrompt]"
    }
}
