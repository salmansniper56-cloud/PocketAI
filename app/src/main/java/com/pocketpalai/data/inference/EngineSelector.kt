package com.pocketpalai.data.inference

import android.content.Context
import java.io.File

object EngineSelector {
    /**
     * Returns the best available local engine for the given model file.
     * Preference order: MLC (GGUF) > ONNX > TensorFlow Lite.
     * If no local asset exists, returns RemoteHfInferenceEngine.
     */
    fun selectEngine(
        context: Context,
        modelMeta: ModelMeta?,
        preferLocal: Boolean = true
    ): InferenceEngine {
        // Prefer local assets when available
        if (preferLocal && modelMeta != null) {
            modelMeta.ggufFile?.let { gguf ->
                val file = File(context.filesDir, "models/${modelMeta.repoId}/$gguf")
                if (file.exists()) return MlcInferenceEngine.getInstance()
            }
            modelMeta.onnxFile?.let { onnx ->
                val file = File(context.filesDir, "models/${modelMeta.repoId}/$onnx")
                if (file.exists()) return OnnxInferenceEngine.getInstance()
            }
            modelMeta.tfliteFile?.let { tflite ->
                val file = File(context.filesDir, "models/${modelMeta.repoId}/$tflite")
                if (file.exists()) return TfliteInferenceEngine.getInstance()
            }
        }
        // Fallback to remote inference (only used if no local asset)
        return RemoteHfInferenceEngine.getInstance()
    }
}
