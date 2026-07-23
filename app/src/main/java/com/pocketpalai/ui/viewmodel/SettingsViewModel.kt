package com.pocketpalai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pocketpalai.data.db.PocketPalDatabase
import com.pocketpalai.data.model.TtsEngine
import com.pocketpalai.data.repository.PocketPalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PocketPalRepository(PocketPalDatabase.getDatabase(application))

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _hfToken = MutableStateFlow("")
    val hfToken: StateFlow<String> = _hfToken.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English (en)")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _autoSpeak = MutableStateFlow(false)
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    private val _ttsEngines = MutableStateFlow<List<TtsEngine>>(emptyList())
    val ttsEngines: StateFlow<List<TtsEngine>> = _ttsEngines.asStateFlow()

    private val _selectedTtsEngineId = MutableStateFlow("kokoro")
    val selectedTtsEngineId: StateFlow<String> = _selectedTtsEngineId.asStateFlow()

    private val _feedbackSubmitted = MutableStateFlow(false)
    val feedbackSubmitted: StateFlow<Boolean> = _feedbackSubmitted.asStateFlow()

    init {
        _ttsEngines.value = repository.getTtsEngines()
    }

    private val _contextSize = MutableStateFlow("2048")
    val contextSize: StateFlow<String> = _contextSize.asStateFlow()

    private val _useMemoryLock = MutableStateFlow(false)
    val useMemoryLock: StateFlow<Boolean> = _useMemoryLock.asStateFlow()

    private val _useMemoryMapping = MutableStateFlow(false)
    val useMemoryMapping: StateFlow<Boolean> = _useMemoryMapping.asStateFlow()

    private val _enableWeightRepacking = MutableStateFlow(false)
    val enableWeightRepacking: StateFlow<Boolean> = _enableWeightRepacking.asStateFlow()

    private val _threads = MutableStateFlow("4")
    val threads: StateFlow<String> = _threads.asStateFlow()

    private val _batchSize = MutableStateFlow("512")
    val batchSize: StateFlow<String> = _batchSize.asStateFlow()

    fun setContextSize(size: String) {
        _contextSize.value = size
    }

    fun toggleMemoryLock(enabled: Boolean) {
        _useMemoryLock.value = enabled
    }

    fun toggleMemoryMapping(enabled: Boolean) {
        _useMemoryMapping.value = enabled
    }

    fun toggleWeightRepacking(enabled: Boolean) {
        _enableWeightRepacking.value = enabled
    }

    fun setThreads(t: String) {
        _threads.value = t
    }

    fun setBatchSize(b: String) {
        _batchSize.value = b
    }

    fun setHfToken(token: String) {
        _hfToken.value = token
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun toggleAutoSpeak(enabled: Boolean) {
        _autoSpeak.value = enabled
    }

    fun selectTtsEngine(engineId: String) {
        _selectedTtsEngineId.value = engineId
    }

    fun submitFeedback(text: String) {
        if (text.isNotBlank()) {
            _feedbackSubmitted.value = true
        }
    }

    fun resetFeedbackStatus() {
        _feedbackSubmitted.value = false
    }
}
