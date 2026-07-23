package com.pocketpalai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketpalai.data.db.PocketPalDatabase
import com.pocketpalai.data.model.*
import com.pocketpalai.data.repository.PocketPalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PocketPalRepository(PocketPalDatabase.getDatabase(application))

    val sessions: StateFlow<List<ChatSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pals: StateFlow<List<Pal>> = repository.allPals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val models: StateFlow<List<LocalModel>> = repository.allModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSessionId = MutableStateFlow<String?>(null)
    val selectedSessionId: StateFlow<String?> = _selectedSessionId.asStateFlow()

    private val _selectedPalId = MutableStateFlow<String>("assistant_default")
    val selectedPalId: StateFlow<String> = _selectedPalId.asStateFlow()

    val currentMessages: StateFlow<List<ChatMessage>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id != null) repository.getMessagesForSession(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    /** Live streaming text from the model — updates token by token during generation */
    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    private val _hfToken = MutableStateFlow("")
    val hfToken: StateFlow<String> = _hfToken.asStateFlow()

    init {
        viewModelScope.launch {
            sessions.collect { sessionList ->
                if (sessionList.isNotEmpty()) {
                    if (_selectedSessionId.value == null) {
                        _selectedSessionId.value = sessionList.first().id
                    }
                } else if (_selectedSessionId.value == null) {
                    val defaultSession = repository.createNewSession("Chat", _selectedPalId.value)
                    _selectedSessionId.value = defaultSession.id
                }
            }
        }
    }

    fun setHfToken(token: String) {
        _hfToken.value = token
    }

    fun selectSession(sessionId: String) {
        _selectedSessionId.value = sessionId
    }

    fun selectPal(palId: String) {
        _selectedPalId.value = palId
    }

    fun selectModel(modelId: String) {
        viewModelScope.launch {
            val targetModel = models.value.find { it.id == modelId }
            if (targetModel?.isDownloaded == true) {
                repository.setLoadedModel(modelId)
            }
        }
    }

    fun startNewChat(title: String = "New Chat") {
        viewModelScope.launch {
            val session = repository.createNewSession(title, _selectedPalId.value)
            _selectedSessionId.value = session.id
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_selectedSessionId.value == sessionId) {
                val remaining = sessions.value.filter { it.id != sessionId }
                _selectedSessionId.value = remaining.firstOrNull()?.id
            }
        }
    }

    private val _isWebSearchEnabled = MutableStateFlow(true)
    val isWebSearchEnabled: StateFlow<Boolean> = _isWebSearchEnabled.asStateFlow()

    fun toggleWebSearch() {
        _isWebSearchEnabled.value = !_isWebSearchEnabled.value
    }

    fun sendMessage(prompt: String) {
        if (prompt.isBlank() || _isGenerating.value) return
        var currentSessionId = _selectedSessionId.value

        viewModelScope.launch {
            _isGenerating.value = true
            _streamingText.value = ""

            if (currentSessionId == null) {
                val session = repository.createNewSession(prompt.take(28), _selectedPalId.value)
                currentSessionId = session.id
                _selectedSessionId.value = currentSessionId
            }

            val currentPal = pals.value.find { it.id == _selectedPalId.value }
            val activeModel = models.value.find { it.isLoaded }

            repository.sendMessage(
                sessionId = currentSessionId!!,
                userPrompt = prompt,
                pal = currentPal,
                activeModel = activeModel,
                hfToken = _hfToken.value,
                enableWebSearch = _isWebSearchEnabled.value,
                onChunk = { partialText ->
                    _streamingText.value = partialText
                }
            )

            _streamingText.value = ""
            _isGenerating.value = false
        }
    }
}
