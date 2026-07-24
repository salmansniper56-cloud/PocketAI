package com.pocketpalai.data.inference

/**
 * Holds metadata for a downloaded model.
 *
 * @param repoId The Hugging Face repository identifier (e.g. "username/model").
 * @param ggufFile Optional GGUF filename for MLC inference.
 * @param onnxFile Optional ONNX filename for ONNX Runtime inference.
 * @param tfliteFile Optional TFLite filename for TensorFlow Lite inference.
 */
data class ModelMeta(
    val repoId: String,
    val ggufFile: String? = null,
    val onnxFile: String? = null,
    val tfliteFile: String? = null
)
