package com.maincharacter.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maincharacter.shared.model.*
import com.maincharacter.shared.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val hostStateRepository: HostStateRepository,
    private val walletRepository: WalletRepository,
    private val signInRepository: SignInRepository,
    private val gachaRepository: GachaRepository,
    private val companionPresentationRepository: CompanionPresentationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            launch { loadHostState() }
            launch { loadWallet() }
            launch { loadSignInState() }
            launch { loadGachaState() }
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
    
    private suspend fun loadSignInState() {
        signInRepository.getSignInState()?.let { state ->
            _uiState.update { it.copy(signInState = state) }
        }
    }
    
    private suspend fun loadGachaState() {
        gachaRepository.getGachaState("pool_normal")?.let { state ->
            _uiState.update { it.copy(gachaState = state) }
        }
    }
    
    private suspend fun loadCompanionPresentation() {
        companionPresentationRepository.getPresentation()?.let { presentation ->
            _uiState.update { it.copy(companionPresentation = presentation) }
        }
    }
    
    fun equipSkin(skinId: String) {
        viewModelScope.launch {
            try {
                companionPresentationRepository.equipSkin(skinId)
                loadCompanionPresentation()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun signIn() {
        viewModelScope.launch {
            try {
                val date = getCurrentDate()
                val result = signInRepository.signIn(date)
                if (result.isSuccess) {
                    loadSignInState()
                    loadWallet()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    private fun getCurrentDate(): String {
        return java.time.LocalDate.now().toString()
    }
}

data class ProfileUiState(
    val hostState: HostState? = null,
    val wallet: Wallet? = null,
    val signInState: SignInState? = null,
    val gachaState: GachaState? = null,
    val companionPresentation: CompanionPresentation? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)