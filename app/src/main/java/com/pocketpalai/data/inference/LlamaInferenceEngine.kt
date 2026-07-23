package com.pocketpalai.data.inference

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Wrapper around the Llamatik library (llama.cpp Kotlin bindings) for
 * running GGUF models on-device.
 *
 * This class manages the full model lifecycle: load, generate, unload.
 * All heavy operations run on Dispatchers.IO.
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
            if (!modelFile.exists()) {
                Log.e(TAG, "Model file not found: $filePath")
                return@withContext false
            }

            Log.d(TAG, "Loading model: $modelName from $filePath (${modelFile.length() / (1024 * 1024)} MB)")

            // Initialize the Llamatik LlamaBridge with the model file
            try {
                com.llamatik.library.platform.LlamaBridge.initGenerateModel(filePath)
                isLoaded = true
                currentModelPath = filePath
                currentModelName = modelName
                Log.d(TAG, "Model loaded successfully: $modelName")
                return@withContext true
            } catch (e: Exception) {
                Log.e(TAG, "Llamatik initGenerateModel failed: ${e.message}", e)
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model: ${e.message}", e)
            return@withContext false
        }
    }

    /**
     * Unload the currently loaded model and free memory.
     */
    suspend fun unloadModel() = withContext(Dispatchers.IO) {
        try {
            if (isLoaded) {
                Log.d(TAG, "Unloading model: $currentModelName")
                try {
                    com.llamatik.library.platform.LlamaBridge.shutdown()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing model via LlamaBridge: ${e.message}")
                }
                isLoaded = false
                currentModelPath = null
                currentModelName = null
                // Suggest GC to reclaim native memory
                System.gc()
                Log.d(TAG, "Model unloaded")
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
     *
     * @param userPrompt The user's input message
     * @param systemPrompt Optional system prompt for persona/instructions
     * @param chatHistory Optional previous chat context for multi-turn conversations
     * @param onToken Called for each generated token (for live streaming UI)
     * @param onComplete Called when generation finishes with the full response
     * @param onError Called if an error occurs during generation
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
            onError(IllegalStateException("No model is loaded. Please load a model first."))
            return@withContext
        }

        try {
            Log.d(TAG, "Starting generation for prompt: ${userPrompt.take(50)}...")
            val fullResponse = StringBuilder()

            // Use Llamatik's streaming generation with context
            val system = systemPrompt ?: "You are a helpful AI assistant. Provide clear, concise answers."
            val context = chatHistory ?: ""

            com.llamatik.library.platform.LlamaBridge.generateWithContextStream(
                system = system,
                context = context,
                user = userPrompt,
                onDelta = { token ->
                    fullResponse.append(token)
                    onToken(token)
                },
                onDone = {
                    val response = fullResponse.toString().trim()
                    Log.d(TAG, "Generation complete. Length: ${response.length} chars")
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
     * Generate a complete response (non-streaming) using the loaded model.
     * Simpler API when streaming UI updates aren't needed.
     */
    suspend fun generate(
        userPrompt: String,
        systemPrompt: String? = null
    ): String = withContext(Dispatchers.IO) {
        if (!isLoaded) {
            return@withContext "Error: No model is loaded. Please download and load a model from the Models screen."
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
