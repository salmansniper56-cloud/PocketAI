package com.pocketpalai.data.inference

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Stub implementation of remote Hugging Face inference.
 * This placeholder satisfies the InferenceEngine interface.
 */
class RemoteHfInferenceEngine private constructor() : InferenceEngine {
    companion object {
        private var INSTANCE: RemoteHfInferenceEngine? = null
        fun getInstance(): RemoteHfInferenceEngine =
            INSTANCE ?: synchronized(this) { INSTANCE ?: RemoteHfInferenceEngine().also { INSTANCE = it } }
    }

    private var loaded = false
    private var modelName: String? = null

    override suspend fun loadModel(filePath: String, modelName: String): Boolean = withContext(Dispatchers.IO) {
        Log.d("RemoteHfInferenceEngine", "loadModel called (stub) for $modelName")
        this@RemoteHfInferenceEngine.loaded = true
        this@RemoteHfInferenceEngine.modelName = modelName
        true
    }

    override suspend fun unloadModel() = withContext(Dispatchers.IO) {
        Log.d("RemoteHfInferenceEngine", "unloadModel (stub)")
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
        val dummy = "[Remote HF stub response for: $userPrompt]"
        onToken(dummy)
        onComplete(dummy)
    }

    override suspend fun generate(
        userPrompt: String,
        systemPrompt: String?,
        chatHistory: String?
    ): String = withContext(Dispatchers.IO) {
        if (!loaded) return@withContext "Error: model not loaded"
        "[Remote HF stub response for: $userPrompt]"
    }
}
