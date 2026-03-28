package com.maincharacter.shared.sync

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.Flow

interface CloudSyncService {
    
    suspend fun syncHostState(state: HostState): Result<Unit>
    suspend fun syncWallet(wallet: Wallet): Result<Unit>
    suspend fun syncInventory(inventory: Inventory): Result<Unit>
    suspend fun syncTaskInstances(instances: List<TaskInstance>): Result<Unit>
    suspend fun syncSignInState(state: SignInState): Result<Unit>
    suspend fun syncGachaState(state: GachaState): Result<Unit>
    suspend fun syncCompanionPresentation(presentation: CompanionPresentation): Result<Unit>
    
    suspend fun pullHostState(): Result<HostState>
    suspend fun pullWallet(): Result<Wallet>
    suspend fun pullInventory(): Result<Inventory>
    suspend fun pullTaskInstances(): Result<List<TaskInstance>>
    suspend fun pullSignInState(): Result<SignInState>
    suspend fun pullGachaState(): Result<GachaState>
    suspend fun pullCompanionPresentation(): Result<CompanionPresentation>
    
    suspend fun syncAll(): Result<Unit>
    suspend fun pullAll(): Result<Unit>
    
    fun observeSyncStatus(): Flow<SyncStatus>
    
    suspend fun getLastSyncTime(): Long
    suspend fun setLastSyncTime(timestamp: Long)
}

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

data class SyncResult(
    val success: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val errors: List<String> = emptyList()
)