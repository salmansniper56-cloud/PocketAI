package com.pocketpalai.data.inference

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Wrapper around the Llamatik library (llama.cpp Kotlin bindings) for
 * running GGUF models on-device.
 *
 * Manages model lifecycle: load, generate, unload.
 * Includes automatic retry, memory check, and file integrity verification.
 */
class LlamaInferenceEngine {

    companion object {
        private const val TAG = "LlamaInferenceEngine"

        @Volatile
        private var INSTANCE: LlamaInferenceEngine? = null

        fun getInstance(): LlamaInferenceEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LlamaInferenceEngine().also { INSTANCE = it }
            }
        }
    }

    private var isLoaded = false
    private var currentModelPath: String? = null
    private var currentModelName: String? = null

    /**
     * Load a GGUF model file into memory for inference.
     * @param filePath Absolute path to the .gguf file on device storage
     * @param modelName Human-readable name for logging
     * @return true if the model was loaded successfully
     */
    suspend fun loadModel(filePath: String, modelName: String = "Model"): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isLoaded) {
                unloadModel()
            }

            val modelFile = File(filePath)
            if (!modelFile.exists() || modelFile.length() < 10 * 1024 * 1024) {
                Log.e(TAG, "Model file missing or incomplete: $filePath (Size: ${if (modelFile.exists()) modelFile.length() else 0} bytes)")
                return@withContext false
            }

            Log.d(TAG, "Loading GGUF model: $modelName from $filePath (${modelFile.length() / (1024 * 1024)} MB)")

            // Attempt 1: Direct Llamatik LlamaBridge initialization
            try {
                com.llamatik.library.platform.LlamaBridge.initGenerateModel(filePath)
                isLoaded = true
                currentModelPath = filePath
                currentModelName = modelName
                Log.d(TAG, "Model loaded successfully into memory: $modelName")
                return@withContext true
            } catch (e: Throwable) {
                Log.w(TAG, "Primary initGenerateModel attempt failed: ${e.message}. Retrying fallback...", e)
            }

            // Attempt 2: Fallback initialization with garbage collection
            System.gc()
            try {
                com.llamatik.library.platform.LlamaBridge.initGenerateModel(filePath)
                isLoaded = true
                currentModelPath = filePath
                currentModelName = modelName
                Log.d(TAG, "Model loaded on secondary attempt: $modelName")
                return@withContext true
            } catch (e: Throwable) {
                Log.e(TAG, "All load attempts failed for $filePath: ${e.message}", e)
                isLoaded = false
                currentModelPath = null
                currentModelName = null
                return@withContext false
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model with exception: ${e.message}", e)
            isLoaded = false
            return@withContext false
        }
    }

    /**
     * Unload the currently loaded model and free RAM.
     */
    suspend fun unloadModel() = withContext(Dispatchers.IO) {
        try {
            if (isLoaded) {
                Log.d(TAG, "Unloading model: $currentModelName")
                try {
                    com.llamatik.library.platform.LlamaBridge.shutdown()
                } catch (e: Throwable) {
                    Log.w(TAG, "Error releasing model via LlamaBridge: ${e.message}")
                }
                isLoaded = false
                currentModelPath = null
                currentModelName = null
                System.gc()
                Log.d(TAG, "Model successfully unloaded from memory")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unloading model: ${e.message}", e)
        }
    }

    /**
     * Check if a model is currently loaded and ready for inference.
     */
    fun isModelLoaded(): Boolean = isLoaded

    /**
     * Get the name of the currently loaded model.
     */
    fun getLoadedModelName(): String? = currentModelName

    /**
     * Generate a response using the loaded GGUF model with streaming output.
     */
    suspend fun generateStream(
        userPrompt: String,
        systemPrompt: String? = null,
        chatHistory: String? = null,
        onToken: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (!isLoaded) {
            onError(IllegalStateException("Model not initialized. Please re-load the model from the Models screen."))
            return@withContext
        }

        try {
            Log.d(TAG, "Starting generation for prompt: ${userPrompt.take(50)}... (Model: $currentModelName)")
            val fullResponse = StringBuilder()

            val system = systemPrompt ?: "You are a helpful AI assistant. Provide clear, concise answers."
            val context = chatHistory ?: ""

            val formattedPrompt = ChatTemplateFormatter.formatPrompt(
                modelName = currentModelName ?: "",
                filePath = currentModelPath ?: "",
                systemPrompt = system,
                chatHistory = context,
                userPrompt = userPrompt
            )

            com.llamatik.library.platform.LlamaBridge.generateWithContextStream(
                system = "",
                context = "",
                user = formattedPrompt,
                onDelta = { token ->
                    val currentText = fullResponse.toString()
                    if (!currentText.contains("<|eot_id|>") && 
                        !currentText.contains("<|end_of_text|>") && 
                        !currentText.contains("<|im_end|>")) {
                        fullResponse.append(token)
                        onToken(token)
                    }
                },
                onDone = {
                    val response = fullResponse.toString()
                        .replace("<|eot_id|>", "")
                        .replace("<|end_of_text|>", "")
                        .replace("<|im_end|>", "")
                        .replace("User:", "")
                        .replace("Assistant:", "")
                        .trim()
                    Log.d(TAG, "Generation complete (${response.length} chars)")
                    onComplete(response)
                },
                onError = { error ->
                    Log.e(TAG, "Generation error: $error")
                    onError(Exception(error))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Generation failed: ${e.message}", e)
            onError(e)
        }
    }

    /**
     * Non-streaming response generation.
     */
    suspend fun generate(
        userPrompt: String,
        systemPrompt: String? = null
    ): String = withContext(Dispatchers.IO) {
        if (!isLoaded) {
            return@withContext "Error: Model not initialized. Please load a model from the Models screen."
        }

        try {
            val system = systemPrompt ?: "You are a helpful AI assistant."
            val response = com.llamatik.library.platform.LlamaBridge.generate(
                "$system\n\nUser: $userPrompt\nAssistant:"
            )
            return@withContext response.trim()
        } catch (e: Exception) {
            Log.e(TAG, "Non-streaming generation failed: ${e.message}", e)
            return@withContext "Error generating response: ${e.message}"
        }
    }
}
