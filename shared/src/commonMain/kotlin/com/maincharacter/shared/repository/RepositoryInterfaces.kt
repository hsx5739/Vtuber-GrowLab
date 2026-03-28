package com.maincharacter.shared.repository

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.Flow

interface HostStateRepository {
    suspend fun getHostState(): HostState?
    suspend fun saveHostState(state: HostState)
    fun observeHostState(): Flow<HostState>
    
    suspend fun updateAttribute(key: StatKey, value: Int)
    suspend fun addBuff(buff: Buff)
    suspend fun removeBuff(buffId: String)
}

interface WalletRepository {
    suspend fun getWallet(): Wallet?
    suspend fun saveWallet(wallet: Wallet)
    fun observeWallet(): Flow<Wallet>
    
    suspend fun addCurrency(currencyId: CurrencyId, amount: Int)
    suspend fun deductCurrency(currencyId: CurrencyId, amount: Int): Boolean
    suspend fun recordTransaction(transaction: Transaction)
}

interface InventoryRepository {
    suspend fun getInventory(): Inventory?
    suspend fun saveInventory(inventory: Inventory)
    fun observeInventory(): Flow<Inventory>
    
    suspend fun addItem(itemId: String, itemType: String, count: Int, data: String = "{}")
    suspend fun removeItem(itemId: String, count: Int): Boolean
    suspend fun equipSkin(skinId: String): Boolean
}

interface TaskRepository {
    suspend fun getTaskInstance(taskId: String): TaskInstance?
    suspend fun getAllTaskInstances(): List<TaskInstance>
    suspend fun saveTaskInstance(instance: TaskInstance)
    fun observeTaskInstance(taskId: String): Flow<TaskInstance?>
    
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus)
    suspend fun completeTask(taskId: String)
    suspend fun verifyTask(taskId: String)
}

interface EventRepository {
    suspend fun getEventOffer(eventId: String): EventOffer?
    suspend fun saveEventOffer(offer: EventOffer)
    fun observeEventOffer(eventId: String): Flow<EventOffer?>
    
    suspend fun selectBranch(eventId: String, branchId: String): Result<RewardBundle>
}

interface SignInRepository {
    suspend fun getSignInState(): SignInState?
    suspend fun saveSignInState(state: SignInState)
    fun observeSignInState(): Flow<SignInState>
    
    suspend fun signIn(date: String): Result<RewardBundle>
    suspend fun checkCanSignIn(date: String): Boolean
}

interface GachaRepository {
    suspend fun getGachaState(poolId: String): GachaState?
    suspend fun saveGachaState(state: GachaState)
    fun observeGachaState(poolId: String): Flow<GachaState?>
    
    suspend fun pull(poolId: String, count: Int): Result<List<RewardBundle>>
    suspend fun getPityCounter(poolId: String): Int
}

interface CompanionPresentationRepository {
    suspend fun getPresentation(): CompanionPresentation?
    suspend fun savePresentation(presentation: CompanionPresentation)
    fun observePresentation(): Flow<CompanionPresentation>
    
    suspend fun equipSkin(skinId: String): Boolean
    suspend fun setIdleVariant(variant: String)
}