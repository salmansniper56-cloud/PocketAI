package com.pocketpalai.data.inference

import android.util.Log

/**
 * Manager that prepares prompts for various GGUF model families, handling any model‑specific quirks.
 * It delegates to the appropriate formatter (currently ChatTemplateFormatter) and can be extended
 * with additional formatters for new architectures.
 */
object ModelCompatibilityManager {
    private const val TAG = "ModelCompatibilityManager"

    /**
     * Prepare a prompt ready for inference.
     *
     * @param modelName Name of the loaded model (usually the filename without extension).
     * @param filePath Absolute path to the GGUF model file.
     * @param systemPrompt System prompt supplied by the UI.
     * @param chatHistory Chat history to include (may be empty).
     * @param userPrompt Current user query.
     * @return A fully‑formatted prompt string.
     */
    fun preparePrompt(
        modelName: String,
        filePath: String,
        systemPrompt: String,
        chatHistory: String,
        userPrompt: String
    ): String {
        // Determine model family based on common identifiers in the filename.
        val family = when {
            modelName.contains("llama", ignoreCase = true) -> "llama"
            modelName.contains("qwen", ignoreCase = true) -> "qwen"
            modelName.contains("gemma", ignoreCase = true) -> "gemma"
            modelName.contains("mistral", ignoreCase = true) -> "mistral"
            modelName.contains("phi", ignoreCase = true) -> "phi"
            modelName.contains("deepseek", ignoreCase = true) -> "deepseek"
            else -> "generic"
        }
        Log.d(TAG, "Detected model family: $family for model: $modelName")

        // For now, all families delegate to the universal ChatTemplateFormatter.
        // Future extensions can switch to family‑specific formatters.
        return try {
            ChatTemplateFormatter.formatPrompt(
                modelName = modelName,
                filePath = filePath,
                systemPrompt = systemPrompt,
                chatHistory = chatHistory,
                userPrompt = userPrompt
            )
        } catch (e: Exception) {
            Log.e(TAG, "Prompt formatting failed: ${e.message}", e)
            // Fallback: simple concatenation to avoid breaking the generation pipeline.
            "${systemPrompt}\n\n${chatHistory}\nUser: $userPrompt"
        }
    }
}
