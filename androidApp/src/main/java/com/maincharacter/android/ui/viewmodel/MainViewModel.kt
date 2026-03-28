package com.maincharacter.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maincharacter.shared.model.*
import com.maincharacter.shared.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val hostStateRepository: HostStateRepository,
    private val walletRepository: WalletRepository,
    private val inventoryRepository: InventoryRepository,
    private val taskRepository: TaskRepository,
    private val signInRepository: SignInRepository,
    private val gachaRepository: GachaRepository,
    private val companionPresentationRepository: CompanionPresentationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            launch { loadHostState() }
            launch { loadWallet() }
            launch { loadInventory() }
            launch { loadTasks() }
            launch { loadSignInState() }
            launch { loadCompanionPresentation() }
        }
    }
    
    private suspend fun loadHostState() {
        hostStateRepository.getHostState()?.let { state ->
            _uiState.update { it.copy(hostState = state) }
        }
    }
    
    private suspend fun loadWallet() {
        walletRepository.getWallet()?.let { wallet ->
            _uiState.update { it.copy(wallet = wallet) }
        }
    }
    
    private suspend fun loadInventory() {
        inventoryRepository.getInventory()?.let { inventory ->
            _uiState.update { it.copy(inventory = inventory) }
        }
    }
    
    private suspend fun loadTasks() {
        val tasks = taskRepository.getAllTaskInstances()
        _uiState.update { it.copy(tasks = tasks) }
    }
    
    private suspend fun loadSignInState() {
        signInRepository.getSignInState()?.let { state ->
            _uiState.update { it.copy(signInState = state) }
        }
    }
    
    private suspend fun loadCompanionPresentation() {
        companionPresentationRepository.getPresentation()?.let { presentation ->
            _uiState.update { it.copy(companionPresentation = presentation) }
        }
    }
    
    fun updateAttribute(key: StatKey, value: Int) {
        viewModelScope.launch {
            hostStateRepository.updateAttribute(key, value)
            loadHostState()
        }
    }
    
    fun completeTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.completeTask(taskId)
            loadTasks()
        }
    }
    
    fun verifyTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.verifyTask(taskId)
            loadTasks()
        }
    }
    
    fun signIn() {
        viewModelScope.launch {
            val date = getCurrentDate()
            val result = signInRepository.signIn(date)
            if (result.isSuccess) {
                loadSignInState()
                loadWallet()
            }
        }
    }
    
    private fun getCurrentDate(): String {
        return java.time.LocalDate.now().toString()
    }
}

data class MainUiState(
    val hostState: HostState? = null,
    val wallet: Wallet? = null,
    val inventory: Inventory? = null,
    val tasks: List<TaskInstance> = emptyList(),
    val signInState: SignInState? = null,
    val companionPresentation: CompanionPresentation? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)