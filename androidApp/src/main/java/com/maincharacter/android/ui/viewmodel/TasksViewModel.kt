package com.maincharacter.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maincharacter.shared.model.*
import com.maincharacter.shared.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasksViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()
    
    init {
        loadTasks()
    }
    
    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val tasks = taskRepository.getAllTaskInstances()
                _uiState.update { 
                    it.copy(
                        tasks = tasks,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun completeTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.completeTask(taskId)
                loadTasks()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun verifyTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.verifyTask(taskId)
                loadTasks()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class TasksUiState(
    val tasks: List<TaskInstance> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)