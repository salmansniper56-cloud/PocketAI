package com.pocketpalai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketpalai.data.db.PocketPalDatabase
import com.pocketpalai.data.download.HuggingFaceDownloader
import com.pocketpalai.data.model.HuggingFaceModel
import com.pocketpalai.data.model.LocalModel
import com.pocketpalai.data.repository.PocketPalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ModelsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PocketPalRepository(PocketPalDatabase.getDatabase(application))
    private val downloader = HuggingFaceDownloader()
    private val downloadJobs = ConcurrentHashMap<String, Job>()

    val localModels: StateFlow<List<LocalModel>> = repository.allModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _hfSearchQuery = MutableStateFlow("")
    val hfSearchQuery: StateFlow<String> = _hfSearchQuery.asStateFlow()

    private val _hfSearchResults = MutableStateFlow<List<HuggingFaceModel>>(emptyList())
    val hfSearchResults: StateFlow<List<HuggingFaceModel>> = _hfSearchResults.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _hfToken = MutableStateFlow("")
    val hfToken: StateFlow<String> = _hfToken.asStateFlow()

    private val _showAddModelDialog = MutableStateFlow(false)
    val showAddModelDialog: StateFlow<Boolean> = _showAddModelDialog.asStateFlow()

    init {
        searchHuggingFace("")
    }

    fun openAddModelDialog() {
        _showAddModelDialog.value = true
    }

    fun closeAddModelDialog() {
        _showAddModelDialog.value = false
    }

    fun setHfToken(token: String) {
        _hfToken.value = token
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        val query = if (category == "All") "" else category
        searchHuggingFace(query)
    }

    fun searchHuggingFace(query: String) {
        _hfSearchQuery.value = query
        viewModelScope.launch {
            _hfSearchResults.value = repository.searchHuggingFace(query)
        }
    }

    /**
     * Load a downloaded model into memory for on-device inference via llama.cpp.
     */
    fun loadModel(modelId: String) {
        viewModelScope.launch {
            val model = localModels.value.find { it.id == modelId }
            if (model?.isDownloaded == true && model.filePath != null) {
                repository.setLoadedModel(modelId)
            }
        }
    }

    /**
     * Unload a model from memory to free RAM.
     */
    fun offloadModel(modelId: String) {
        viewModelScope.launch {
            repository.offloadModel(modelId)
        }
    }

    /**
     * Start a real download for a pre-seeded LocalModel from the database.
     * This is used when the user taps "Download" on a model card in the Available section.
     */
    fun startDownloadForModel(model: LocalModel) {
        viewModelScope.launch {
            val db = PocketPalDatabase.getDatabase(getApplication())
            db.localModelDao().insertModel(model.copy(downloadStatus = "DOWNLOADING", downloadProgress = 0))
            runRealDownload(model)
        }
    }

    fun deleteModel(modelId: String) {
        viewModelScope.launch {
            // Cancel any active download
            downloadJobs[modelId]?.cancel()
            downloadJobs.remove(modelId)

            // Offload if loaded
            val model = localModels.value.find { it.id == modelId }
            if (model?.isLoaded == true) {
                repository.offloadModel(modelId)
            }

            // Delete the downloaded file
            model?.filePath?.let { path ->
                try {
                    File(path).delete()
                } catch (_: Exception) {}
            }

            val db = PocketPalDatabase.getDatabase(getApplication())
            db.localModelDao().deleteModelById(modelId)
        }
    }

    /**
     * Start a real download of a GGUF model from HuggingFace.
     */
    fun startHfDownload(hfModel: HuggingFaceModel, quant: String) {
        val modelId = "model_" + hfModel.id.replace("/", "_").lowercase() + "_" + quant.lowercase()
        val newLocalModel = LocalModel(
            id = modelId,
            name = "${hfModel.name} ($quant)",
            repo = hfModel.id,
            sizeBytes = hfModel.sizeBytes,
            quantization = quant,
            isDownloaded = false,
            isLoaded = false,
            contextLength = 4096,
            parameters = hfModel.parameters,
            downloadProgress = 0,
            downloadStatus = "DOWNLOADING",
            ramRequiredMb = hfModel.ramRequiredMb,
            author = hfModel.author,
            description = hfModel.description.ifEmpty { "GGUF model downloaded from Hugging Face." }
        )

        viewModelScope.launch {
            val db = PocketPalDatabase.getDatabase(getApplication())
            db.localModelDao().insertModel(newLocalModel)
            runRealDownload(newLocalModel)
        }
    }

    /**
     * Start a download from a custom HuggingFace repo ID or direct URL.
     */
    fun startCustomHfDownload(repoOrUrl: String, quant: String, token: String) {
        if (repoOrUrl.isBlank()) return
        val cleanRepo = repoOrUrl.trim().removePrefix("https://huggingface.co/")
        val repoName = cleanRepo.substringAfterLast("/")
        val author = if (cleanRepo.contains("/")) cleanRepo.substringBefore("/") else "Custom"
        val modelId = "custom_" + cleanRepo.replace("/", "_").lowercase() + "_" + quant.lowercase()

        val customModel = LocalModel(
            id = modelId,
            name = "$repoName ($quant)",
            repo = cleanRepo,
            sizeBytes = 1_800_000_000L,
            quantization = quant,
            isDownloaded = false,
            isLoaded = false,
            contextLength = 4096,
            parameters = "3B",
            downloadProgress = 0,
            downloadStatus = "DOWNLOADING",
            ramRequiredMb = 2000,
            author = author,
            description = "Custom GGUF model from Hugging Face ($cleanRepo)."
        )

        if (token.isNotBlank()) {
            _hfToken.value = token
        }

        viewModelScope.launch {
            val db = PocketPalDatabase.getDatabase(getApplication())
            db.localModelDao().insertModel(customModel)
            runRealDownload(customModel)
        }
    }

    fun pauseDownload(model: LocalModel) {
        downloadJobs[model.id]?.cancel()
        downloadJobs.remove(model.id)
        viewModelScope.launch {
            val db = PocketPalDatabase.getDatabase(getApplication())
            db.localModelDao().insertModel(model.copy(downloadStatus = "PAUSED"))
        }
    }

    fun resumeDownload(model: LocalModel) {
        viewModelScope.launch {
            val db = PocketPalDatabase.getDatabase(getApplication())
            db.localModelDao().insertModel(model.copy(downloadStatus = "DOWNLOADING"))
            runRealDownload(model)
        }
    }

    fun cancelDownload(model: LocalModel) {
        downloadJobs[model.id]?.cancel()
        downloadJobs.remove(model.id)
        viewModelScope.launch {
            // Delete partial file
            model.filePath?.let { path ->
                try { File(path).delete() } catch (_: Exception) {}
            }
            val db = PocketPalDatabase.getDatabase(getApplication())
            db.localModelDao().deleteModelById(model.id)
        }
    }

    /**
     * Performs the actual HTTP download of a GGUF file from HuggingFace.
     * Updates the Room database with real progress as bytes are received.
     */
    private fun runRealDownload(model: LocalModel) {
        downloadJobs[model.id]?.cancel()

        val job = viewModelScope.launch(Dispatchers.IO) {
            val db = PocketPalDatabase.getDatabase(getApplication())
            val modelsDir = File(getApplication<Application>().filesDir, "models")

            try {
                val downloadedFile = downloader.downloadModel(
                    repoId = model.repo,
                    quantization = model.quantization,
                    outputDir = modelsDir,
                    hfToken = _hfToken.value.ifBlank { null },
                    existingBytes = model.downloadedBytes,
                    onProgress = { progress, bytesDownloaded, totalBytes ->
                        db.localModelDao().insertModel(
                            model.copy(
                                downloadProgress = progress,
                                downloadedBytes = bytesDownloaded,
                                sizeBytes = if (totalBytes > 0) totalBytes else model.sizeBytes,
                                downloadStatus = if (progress >= 100) "COMPLETED" else "DOWNLOADING",
                                isDownloaded = progress >= 100,
                                filePath = if (progress >= 100) File(modelsDir, model.repo.replace("/", "_").lowercase() + "_${model.quantization.lowercase()}.gguf").absolutePath else null
                            )
                        )
                    }
                )

                if (downloadedFile != null) {
                    // Mark as completed with the actual file path
                    db.localModelDao().insertModel(
                        model.copy(
                            downloadProgress = 100,
                            downloadedBytes = downloadedFile.length(),
                            sizeBytes = downloadedFile.length(),
                            downloadStatus = "COMPLETED",
                            isDownloaded = true,
                            filePath = downloadedFile.absolutePath
                        )
                    )
                } else {
                    // Download failed
                    db.localModelDao().insertModel(
                        model.copy(
                            downloadStatus = "ERROR",
                            description = model.description + " [Download failed — check internet or try a different quantization]"
                        )
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Download was paused/cancelled — handled by pause/cancel functions
            } catch (e: Exception) {
                db.localModelDao().insertModel(
                    model.copy(
                        downloadStatus = "ERROR",
                        description = "Download error: ${e.message}"
                    )
                )
            }

            downloadJobs.remove(model.id)
        }
        downloadJobs[model.id] = job
    }
}
