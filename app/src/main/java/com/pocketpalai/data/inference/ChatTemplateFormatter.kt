package com.pocketpalai.data.inference

/**
 * Universal Chat Template Formatter for GGUF models.
 * Automatically formats prompts for Llama 3, Qwen 2.5, Gemma 2, DeepSeek R1, Phi 3, and Mistral architectures.
 */
object ChatTemplateFormatter {

    enum class ModelFamily {
        LLAMA_3,
        QWEN_CHATML,
        GEMMA_2,
        MISTRAL_INST,
        GENERIC
    }

    fun detectModelFamily(modelName: String, filePath: String): ModelFamily {
        val nameLower = "$modelName $filePath".lowercase()
        return when {
            nameLower.contains("llama-3") || nameLower.contains("llama3") -> ModelFamily.LLAMA_3
            nameLower.contains("qwen") || nameLower.contains("deepseek") || nameLower.contains("phi") || nameLower.contains("smollm") -> ModelFamily.QWEN_CHATML
            nameLower.contains("gemma") -> ModelFamily.GEMMA_2
            nameLower.contains("mistral") || nameLower.contains("mixtral") || nameLower.contains("llama-2") -> ModelFamily.MISTRAL_INST
            else -> ModelFamily.GENERIC
        }
    }

    fun formatPrompt(
        modelName: String,
        filePath: String,
        systemPrompt: String,
        chatHistory: String,
        userPrompt: String
    ): String {
        val family = detectModelFamily(modelName, filePath)
        val combinedUser = if (chatHistory.isNotBlank()) "$chatHistory\nUser: $userPrompt" else userPrompt

        return when (family) {
            ModelFamily.LLAMA_3 -> {
                "<|start_header_id|>system<|end_header_id|>\n\n$systemPrompt<|eot_id|><|start_header_id|>user<|end_header_id|>\n\n$combinedUser<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n"
            }
            ModelFamily.QWEN_CHATML -> {
                "<|im_start|>system\n$systemPrompt<|im_end|>\n<|im_start|>user\n$combinedUser<|im_end|>\n<|im_start|>assistant\n"
            }
            ModelFamily.GEMMA_2 -> {
                "<start_of_turn>user\n$systemPrompt\n\n$combinedUser<end_of_turn>\n<start_of_turn>model\n"
            }
            ModelFamily.MISTRAL_INST -> {
                "[INST] <<SYS>>\n$systemPrompt\n<</SYS>>\n\n$combinedUser [/INST]"
            }
            ModelFamily.GENERIC -> {
                "$systemPrompt\n\n$combinedUser\n\nAssistant:"
            }
        }
    }
}
