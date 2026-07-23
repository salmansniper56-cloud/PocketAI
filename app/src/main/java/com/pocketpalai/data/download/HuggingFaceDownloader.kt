package com.pocketpalai.data.download

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

/**
 * Downloads GGUF model files from HuggingFace repositories.
 *
 * Features:
 * - Strips Authorization headers on S3/CDN redirects to avoid AWS 400 Bad Request
 * - Queries Hugging Face API first for exact rfilename lookup
 * - Comprehensive candidate filename URL resolution
 * - Resume support via HTTP Range headers
 * - Real-time progress updates & memory-efficient stream writing
 */
class HuggingFaceDownloader {

    companion object {
        private const val TAG = "HuggingFaceDownloader"
        private const val BUFFER_SIZE = 65536

        private val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.MINUTES)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 PocketAI/1.0")

                // Strip Authorization header if redirected to S3 / CDN domain
                val host = originalRequest.url.host.lowercase()
                if (host.contains("cdn-lfs") || host.contains("amazonaws") || host.contains("cloudflare")) {
                    requestBuilder.removeHeader("Authorization")
                }

                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    /**
     * Download a GGUF model file from HuggingFace to the specified output directory.
     */
    suspend fun downloadModel(
        repoId: String,
        quantization: String,
        outputDir: File,
        hfToken: String? = null,
        existingBytes: Long = 0,
        onProgress: suspend (Int, Long, Long) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            // Direct URL support
            if (repoId.startsWith("http://") || repoId.startsWith("https://")) {
                val urlFileName = repoId.substringAfterLast("/")
                val outputFile = File(outputDir, if (urlFileName.endsWith(".gguf", ignoreCase = true)) urlFileName else "custom_model.gguf")
                Log.d(TAG, "Downloading direct URL: $repoId")
                return@withContext tryDownloadUrl(repoId, outputFile, hfToken, existingBytes, onProgress)
            }

            val localFileName = repoId.replace("/", "_").lowercase() + "_${quantization.lowercase()}.gguf"
            val outputFile = File(outputDir, localFileName)

            // Strategy 1: Query Hugging Face API for exact filename
            Log.d(TAG, "Querying HF API for exact filename in repo: $repoId")
            val apiResult = tryFallbackDownload(repoId, quantization, outputDir, hfToken, onProgress)
            if (apiResult != null) {
                return@withContext apiResult
            }

            // Strategy 2: Candidate filename guessing
            val repoName = repoId.substringAfter("/")
            val baseName = repoName.removeSuffix("-GGUF").removeSuffix("-gguf").removeSuffix("-Gguf")
            val baseNameLower = baseName.lowercase()
            val repoNameLower = repoName.lowercase()
            val quantUpper = quantization.uppercase()
            val quantLower = quantization.lowercase()

            val candidateFileNames = listOf(
                "$baseName-$quantUpper.gguf",
                "$baseNameLower-$quantLower.gguf",
                "$baseName.$quantUpper.gguf",
                "$baseNameLower.$quantLower.gguf",
                "$baseName-$quantLower.gguf",
                "$baseNameLower-$quantUpper.gguf",
                "$repoName-$quantUpper.gguf",
                "$repoNameLower-$quantLower.gguf",
                "$baseName.gguf",
                "$baseNameLower.gguf"
            ).distinct()

            for (candidateName in candidateFileNames) {
                val candidateUrl = "https://huggingface.co/$repoId/resolve/main/$candidateName"
                Log.d(TAG, "Testing candidate URL: $candidateUrl")

                val result = tryDownloadUrl(candidateUrl, outputFile, hfToken, existingBytes, onProgress)
                if (result != null) {
                    return@withContext result
                }
            }

            Log.e(TAG, "All download strategies failed for repo: $repoId, quant: $quantization")
            return@withContext null

        } catch (e: kotlinx.coroutines.CancellationException) {
            Log.d(TAG, "Download cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Download failed with exception: ${e.message}", e)
            return@withContext null
        }
    }

    private suspend fun tryDownloadUrl(
        url: String,
        outputFile: File,
        hfToken: String?,
        existingBytes: Long,
        onProgress: suspend (Int, Long, Long) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder().url(url)
            if (!hfToken.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $hfToken")
            }
            if (existingBytes > 0 && outputFile.exists() && outputFile.length() == existingBytes) {
                requestBuilder.addHeader("Range", "bytes=$existingBytes-")
            }

            val request = requestBuilder.build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.d(TAG, "URL $url returned HTTP ${response.code}")
                response.close()
                return@withContext null
            }

            val body = response.body ?: run {
                response.close()
                return@withContext null
            }

            val contentLength = body.contentLength()
            val totalBytes = if (contentLength > 0) contentLength + existingBytes else -1L
            var downloadedBytes = existingBytes

            val fos = if (existingBytes > 0) FileOutputStream(outputFile, true) else FileOutputStream(outputFile)

            var lastReportedProgress = -1
            var lastReportedTime = 0L

            body.byteStream().use { inputStream ->
                fos.use { outputStream ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        coroutineContext.ensureActive()
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        val progress = if (totalBytes > 0) {
                            ((downloadedBytes.toDouble() / totalBytes) * 100).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }

                        val now = System.currentTimeMillis()
                        if (progress != lastReportedProgress && (now - lastReportedTime > 300 || progress == 100)) {
                            lastReportedProgress = progress
                            lastReportedTime = now
                            onProgress(progress, downloadedBytes, totalBytes)
                        }
                    }
                }
            }

            if (outputFile.exists() && outputFile.length() > 0) {
                Log.d(TAG, "Successfully downloaded: ${outputFile.absolutePath} (${outputFile.length() / (1024 * 1024)} MB)")
                return@withContext outputFile
            } else {
                return@withContext null
            }

        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Download attempt failed for $url: ${e.message}")
            return@withContext null
        }
    }

    private suspend fun tryFallbackDownload(
        repoId: String,
        quantization: String,
        outputDir: File,
        hfToken: String?,
        onProgress: suspend (Int, Long, Long) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://huggingface.co/api/models/$repoId"
            val requestBuilder = Request.Builder().url(apiUrl)
            if (!hfToken.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $hfToken")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "HF API listing failed with HTTP ${response.code}")
                response.close()
                return@withContext null
            }

            val responseBody = response.body?.string()
            response.close()
            if (responseBody.isNullOrBlank()) return@withContext null

            // Extract all rfilename occurrences from HF JSON response
            val regex = Regex(""""rfilename"\s*:\s*"([^"]+)"""")
            val matches = regex.findAll(responseBody).map { it.groupValues[1] }.toList()

            val ggufFiles = matches.filter { it.endsWith(".gguf", ignoreCase = true) }
            Log.d(TAG, "Found ${ggufFiles.size} GGUF files in repo $repoId: $ggufFiles")

            val quantClean = quantization.lowercase().replace("_", "").replace("-", "")
            val matchedFile = ggufFiles.firstOrNull { file ->
                val fClean = file.lowercase().replace("_", "").replace("-", "")
                fClean.contains(quantClean)
            } ?: ggufFiles.firstOrNull { file ->
                file.contains(quantization, ignoreCase = true)
            } ?: ggufFiles.firstOrNull()

            if (matchedFile != null) {
                Log.d(TAG, "Downloading matched GGUF file from HF API: $matchedFile")
                val directUrl = "https://huggingface.co/$repoId/resolve/main/$matchedFile"
                val localFileName = repoId.replace("/", "_").lowercase() + "_${quantization.lowercase()}.gguf"
                val outputFile = File(outputDir, localFileName)
                return@withContext tryDownloadUrl(directUrl, outputFile, hfToken, 0L, onProgress)
            }

            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Fallback download error: ${e.message}", e)
            return@withContext null
        }
    }
}
