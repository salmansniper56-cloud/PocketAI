package com.pocketpalai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketpalai.data.db.PocketPalDatabase
import com.pocketpalai.data.model.Pal
import com.pocketpalai.data.repository.PocketPalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class PalsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PocketPalRepository(PocketPalDatabase.getDatabase(application))

    val myPals: StateFlow<List<Pal>> = repository.allPals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _marketplacePals = MutableStateFlow<List<Pal>>(emptyList())
    val marketplacePals: StateFlow<List<Pal>> = _marketplacePals.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        _marketplacePals.value = repository.getPalsHubMarketplace()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun savePal(
        name: String,
        type: String,
        systemPrompt: String,
        avatar: String,
        colorHex: String,
        role: String,
        location: String,
        enableTools: Boolean
    ) {
        viewModelScope.launch {
            val newPal = Pal(
                id = UUID.randomUUID().toString(),
                name = name.ifEmpty { "Custom Pal" },
                type = type,
                systemPrompt = systemPrompt,
                avatar = avatar.ifEmpty { "🤖" },
                colorHex = colorHex,
                role = role,
                location = location,
                enableTools = enableTools,
                isPreset = false
            )
            repository.savePal(newPal)
        }
    }

    fun installFromMarketplace(pal: Pal) {
        viewModelScope.launch {
            repository.savePal(pal.copy(id = "installed_${UUID.randomUUID()}", isPreset = false))
        }
    }

    fun deletePal(id: String) {
        viewModelScope.launch {
            repository.deletePal(id)
        }
    }
}
