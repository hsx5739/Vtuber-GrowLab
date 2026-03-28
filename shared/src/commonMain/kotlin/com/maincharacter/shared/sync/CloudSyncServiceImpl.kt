package com.maincharacter.shared.sync

import com.maincharacter.shared.model.*
import com.maincharacter.shared.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CloudSyncServiceImpl(
    private val hostStateRepository: HostStateRepository,
    private val walletRepository: WalletRepository,
    private val inventoryRepository: InventoryRepository,
    private val taskRepository: TaskRepository,
    private val signInRepository: SignInRepository,
    private val gachaRepository: GachaRepository,
    private val companionPresentationRepository: CompanionPresentationRepository
) : CloudSyncService {
    
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    override fun observeSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()
    
    private var lastSyncTime: Long = 0
    
    override suspend fun syncHostState(state: HostState): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            
            hostStateRepository.saveHostState(state)
            
            _syncStatus.value = SyncStatus.SUCCESS
            lastSyncTime = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }
    
    override suspend fun syncWallet(wallet: Wallet): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            
            walletRepository.saveWallet(wallet)
            
            _syncStatus.value = SyncStatus.SUCCESS
            lastSyncTime = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }
    
    override suspend fun syncInventory(inventory: Inventory): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            
            inventoryRepository.saveInventory(inventory)
            
            _syncStatus.value = SyncStatus.SUCCESS
            lastSyncTime = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }
    
    override suspend fun syncTaskInstances(instances: List<TaskInstance>): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            
            instances.forEach { instance ->
                taskRepository.saveTaskInstance(instance)
            }
            
            _syncStatus.value = SyncStatus.SUCCESS
            lastSyncTime = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }
    
    override suspend fun syncSignInState(state: SignInState): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            
            signInRepository.saveSignInState(state)
            
            _syncStatus.value = SyncStatus.SUCCESS
            lastSyncTime = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }
    
    override suspend fun syncGachaState(state: GachaState): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            
            gachaRepository.saveGachaState(state)
            
            _syncStatus.value = SyncStatus.SUCCESS
            lastSyncTime = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }
    
    override suspend fun syncCompanionPresentation(presentation: CompanionPresentation): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            
            companionPresentationRepository.savePresentation(presentation)
            
            _syncStatus.value = SyncStatus.SUCCESS
            lastSyncTime = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }
    
    override suspend fun pullHostState(): Result<HostState> {
        return try {
            val state = hostStateRepository.getHostState()
            if (state != null) {
                Result.success(state)
            } else {
                Result.failure(IllegalStateException("Host state not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun pullWallet(): Result<Wallet> {
        return try {
            val wallet = walletRepository.getWallet()
            if (wallet != null) {
                Result.success(wallet)
            } else {
                Result.failure(IllegalStateException("Wallet not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun pullInventory(): Result<Inventory> {
        return try {
            val inventory = inventoryRepository.getInventory()
            if (inventory != null) {
                Result.success(inventory)
            } else {
                Result.failure(IllegalStateException("Inventory not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun pullTaskInstances(): Result<List<TaskInstance>> {
        return try {
            val instances = taskRepository.getAllTaskInstances()
            Result.success(instances)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun pullSignInState(): Result<SignInState> {
        return try {
            val state = signInRepository.getSignInState()
            if (state != null) {
                Result.success(state)
            } else {
                Result.failure(IllegalStateException("Sign-in state not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun pullGachaState(): Result<GachaState> {
        return try {
            val state = gachaRepository.getGachaState("pool_normal")
            if (state != null) {
                Result.success(state)
            } else {
                Result.failure(IllegalStateException("Gacha state not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun pullCompanionPresentation(): Result<CompanionPresentation> {
        return try {
            val presentation = companionPresentationRepository.getPresentation()
            if (presentation != null) {
                Result.success(presentation)
            } else {
                Result.failure(IllegalStateException("Companion presentation not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun syncAll(): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            
            val hostState = pullHostState().getOrThrow()
            val wallet = pullWallet().getOrThrow()
            val inventory = pullInventory().getOrThrow()
            val taskInstances = pullTaskInstances().getOrThrow()
            val signInState = pullSignInState().getOrThrow()
            val gachaState = pullGachaState().getOrThrow()
            val presentation = pullCompanionPresentation().getOrThrow()
            
            syncHostState(hostState)
            syncWallet(wallet)
            syncInventory(inventory)
            syncTaskInstances(taskInstances)
            syncSignInState(signInState)
            syncGachaState(gachaState)
            syncCompanionPresentation(presentation)
            
            _syncStatus.value = SyncStatus.SUCCESS
            lastSyncTime = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }
    
    override suspend fun pullAll(): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            
            pullHostState()
            pullWallet()
            pullInventory()
            pullTaskInstances()
            pullSignInState()
            pullGachaState()
            pullCompanionPresentation()
            
            _syncStatus.value = SyncStatus.SUCCESS
            lastSyncTime = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }
    
    override suspend fun getLastSyncTime(): Long {
        return lastSyncTime
    }
    
    override suspend fun setLastSyncTime(timestamp: Long) {
        lastSyncTime = timestamp
    }
}
