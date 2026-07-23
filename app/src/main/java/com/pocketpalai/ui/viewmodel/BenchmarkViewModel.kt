package com.pocketpalai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketpalai.data.db.PocketPalDatabase
import com.pocketpalai.data.model.BenchmarkResult
import com.pocketpalai.data.repository.PocketPalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BenchmarkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PocketPalRepository(PocketPalDatabase.getDatabase(application))

    val benchmarkHistory: StateFlow<List<BenchmarkResult>> = repository.allBenchmarkResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRunningTest = MutableStateFlow(false)
    val isRunningTest: StateFlow<Boolean> = _isRunningTest.asStateFlow()

    private val _latestResult = MutableStateFlow<BenchmarkResult?>(null)
    val latestResult: StateFlow<BenchmarkResult?> = _latestResult.asStateFlow()

    fun runDeviceBenchmark(modelName: String = "Gemma 2B IT (Q4_K_M)") {
        if (_isRunningTest.value) return
        viewModelScope.launch {
            _isRunningTest.value = true
            val result = repository.runBenchmark(modelName)
            _latestResult.value = result
            _isRunningTest.value = false
        }
    }
}
