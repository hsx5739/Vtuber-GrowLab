package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OfflineDegradationTest {
    
    @Test
    fun testOfflineSignIn() {
        val offlineManager = TestOfflineManager()
        val userId = "user_offline_001"
        
        offlineManager.setOnline(false)
        
        val signInResult = offlineManager.signIn(userId)
        assertTrue(signInResult.success)
        assertTrue(signInResult.isOffline)
        assertEquals("Offline sign-in successful", signInResult.message)
        
        val cachedData = offlineManager.getCachedData(userId)
        assertNotNull(cachedData)
    }
    
    @Test
    fun testOfflineTaskCompletion() {
        val offlineManager = TestOfflineManager()
        val taskManager = TestTaskManager()
        val userId = "user_offline_002"
        
        offlineManager.setOnline(false)
        
        val taskId = "task_offline_001"
        val completeResult = offlineManager.completeTaskOffline(userId, taskId, taskManager)
        assertTrue(completeResult.success)
        assertTrue(completeResult.isCached)
        
        val cachedTasks = offlineManager.getCachedTaskCompletions(userId)
        assertTrue(cachedTasks.contains(taskId))
    }
    
    @Test
    fun testOfflineRewardClaim() {
        val offlineManager = TestOfflineManager()
        val rewardManager = TestRewardManager()
        val userId = "user_offline_003"
        
        offlineManager.setOnline(false)
        
        val rewardId = "reward_offline_001"
        val claimResult = offlineManager.claimRewardOffline(userId, rewardId, rewardManager)
        assertTrue(claimResult.success)
        assertTrue(claimResult.isCached)
        
        val cachedRewards = offlineManager.getCachedRewardClaims(userId)
        assertTrue(cachedRewards.contains(rewardId))
    }
    
    @Test
    fun testOfflineChat() {
        val offlineManager = TestOfflineManager()
        val chatManager = TestChatManager()
        val userId = "user_offline_004"
        
        offlineManager.setOnline(false)
        
        val message = "Hello offline"
        val sendResult = offlineManager.sendMessageOffline(userId, message, chatManager)
        assertTrue(sendResult.success)
        assertTrue(sendResult.isCached)
        
        val cachedMessages = offlineManager.getCachedMessages(userId)
        assertTrue(cachedMessages.isNotEmpty())
    }
    
    @Test
    fun testOfflineGachaPull() {
        val offlineManager = TestOfflineManager()
        val gachaManager = TestGachaManager()
        val userId = "user_offline_005"
        
        offlineManager.setOnline(false)
        
        val pullResult = offlineManager.pullGachaOffline(userId, "pool_normal", gachaManager)
        assertFalse(pullResult.success)
        assertEquals("Gacha not available offline", pullResult.errorMessage)
    }
    
    @Test
    fun testOnlineSync() {
        val offlineManager = TestOfflineManager()
        val taskManager = TestTaskManager()
        val userId = "user_offline_006"
        
        offlineManager.setOnline(false)
        
        val taskId = "task_offline_002"
        offlineManager.completeTaskOffline(userId, taskId, taskManager)
        
        offlineManager.setOnline(true)
        
        val syncResult = offlineManager.syncData(userId, taskManager)
        assertTrue(syncResult.success)
        assertEquals(1, syncResult.syncedTasks)
    }
    
    @Test
    fun testOfflineDataPersistence() {
        val offlineManager = TestOfflineManager()
        val userId = "user_offline_007"
        
        offlineManager.setOnline(false)
        
        val data = mapOf("key1" to "value1", "key2" to "value2")
        offlineManager.cacheData(userId, data)
        
        val cachedData = offlineManager.getCachedData(userId)
        assertNotNull(cachedData)
        assertEquals(data, cachedData)
    }
    
    @Test
    fun testOfflineEventTriggering() {
        val offlineManager = TestOfflineManager()
        val eventManager = TestEventManager()
        val userId = "user_offline_008"
        
        offlineManager.setOnline(false)
        
        val triggerResult = offlineManager.triggerEventOffline(userId, "evt_offline_001", eventManager)
        assertFalse(triggerResult.success)
        assertEquals("Events not available offline", triggerResult.errorMessage)
    }
    
    @Test
    fun testOfflineCurrencyUpdate() {
        val offlineManager = TestOfflineManager()
        val currencyManager = TestCurrencyManager()
        val userId = "user_offline_009"
        
        offlineManager.setOnline(false)
        
        val currencyType = CurrencyType.STAR_DUST
        val amount = 100
        val updateResult = offlineManager.updateCurrencyOffline(userId, currencyType, amount, currencyManager)
        assertTrue(updateResult.success)
        assertTrue(updateResult.isCached)
        
        val cachedUpdates = offlineManager.getCachedCurrencyUpdates(userId)
        assertTrue(cachedUpdates.isNotEmpty())
    }
    
    @Test
    fun testOfflineErrorHandling() {
        val offlineManager = TestOfflineManager()
        val userId = "user_offline_010"
        
        offlineManager.setOnline(false)
        
        val result = offlineManager.performOfflineOperation(userId)
        assertFalse(result.success)
        assertEquals("Operation failed offline", result.errorMessage)
        assertTrue(result.isOffline)
    }
    
    @Test
    fun testOfflineModeDetection() {
        val offlineManager = TestOfflineManager()
        
        offlineManager.setOnline(false)
        assertTrue(offlineManager.isOffline())
        
        offlineManager.setOnline(true)
        assertFalse(offlineManager.isOffline())
    }
    
    @Test
    fun testOfflineQueueProcessing() {
        val offlineManager = TestOfflineManager()
        val taskManager = TestTaskManager()
        val userId = "user_offline_011"
        
        offlineManager.setOnline(false)
        
        val task1Id = "task_offline_003"
        val task2Id = "task_offline_004"
        
        offlineManager.completeTaskOffline(userId, task1Id, taskManager)
        offlineManager.completeTaskOffline(userId, task2Id, taskManager)
        
        val queueSize = offlineManager.getOfflineQueueSize(userId)
        assertEquals(2, queueSize)
        
        offlineManager.setOnline(true)
        
        val syncResult = offlineManager.syncData(userId, taskManager)
        assertTrue(syncResult.success)
        assertEquals(2, syncResult.syncedTasks)
        
        val finalQueueSize = offlineManager.getOfflineQueueSize(userId)
        assertEquals(0, finalQueueSize)
    }
    
    @Test
    fun testOfflineDataExpiration() {
        val offlineManager = TestOfflineManager()
        val userId = "user_offline_012"
        
        offlineManager.setOnline(false)
        
        val oldData = mapOf("old_key" to "old_value")
        offlineManager.cacheDataWithExpiration(userId, oldData, 1)
        
        val newData = mapOf("new_key" to "new_value")
        offlineManager.cacheDataWithExpiration(userId, newData, 24)
        
        val cachedData = offlineManager.getCachedData(userId)
        assertNotNull(cachedData)
        assertTrue(cachedData.containsKey("new_key"))
        assertFalse(cachedData.containsKey("old_key"))
    }
}

class TestOfflineManager : OfflineManager {
    private val _isOnline = MutableStateFlow(true)
    private val _cachedData = MutableStateFlow<Map<String, Map<String, Any>>>(emptyMap())
    private val _cachedTaskCompletions = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    private val _cachedRewardClaims = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    private val _cachedMessages = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    private val _cachedCurrencyUpdates = MutableStateFlow<Map<String, List<CurrencyUpdate>>>(emptyMap())
    
    override fun isOnline(): Boolean {
        return _isOnline.value
    }
    
    override fun setOnline(isOnline: Boolean) {
        _isOnline.value = isOnline
    }
    
    override fun isOffline(): Boolean {
        return !_isOnline.value
    }
    
    override fun signIn(userId: String): OfflineResult {
        if (isOnline()) {
            return OfflineResult(
                success = true,
                isOffline = false,
                message = "Online sign-in successful"
            )
        }
        
        val cachedData = getCachedData(userId)
        if (cachedData == null) {
            return OfflineResult(
                success = false,
                isOffline = true,
                errorMessage = "No cached data available"
            )
        }
        
        return OfflineResult(
            success = true,
            isOffline = true,
            message = "Offline sign-in successful"
        )
    }
    
    override fun completeTaskOffline(userId: String, taskId: String, taskManager: Any): OfflineResult {
        if (isOnline()) {
            return OfflineResult(
                success = false,
                isOffline = false,
                errorMessage = "Operation should be done online"
            )
        }
        
        val updatedCompletions = _cachedTaskCompletions.value.toMutableMap()
        val userCompletions = updatedCompletions[userId]?.toMutableSet() ?: mutableSetOf()
        userCompletions.add(taskId)
        updatedCompletions[userId] = userCompletions
        _cachedTaskCompletions.value = updatedCompletions
        
        return OfflineResult(
            success = true,
            isCached = true,
            message = "Task completion cached"
        )
    }
    
    override fun claimRewardOffline(userId: String, rewardId: String, rewardManager: Any): OfflineResult {
        if (isOnline()) {
            return OfflineResult(
                success = false,
                isOffline = false,
                errorMessage = "Operation should be done online"
            )
        }
        
        val updatedClaims = _cachedRewardClaims.value.toMutableMap()
        val userClaims = updatedClaims[userId]?.toMutableSet() ?: mutableSetOf()
        userClaims.add(rewardId)
        updatedClaims[userId] = userClaims
        _cachedRewardClaims.value = updatedClaims
        
        return OfflineResult(
            success = true,
            isCached = true,
            message = "Reward claim cached"
        )
    }
    
    override fun sendMessageOffline(userId: String, message: String, chatManager: Any): OfflineResult {
        if (isOnline()) {
            return OfflineResult(
                success = false,
                isOffline = false,
                errorMessage = "Operation should be done online"
            )
        }
        
        val updatedMessages = _cachedMessages.value.toMutableMap()
        val userMessages = updatedMessages[userId]?.toMutableList() ?: mutableListOf()
        userMessages.add(message)
        updatedMessages[userId] = userMessages
        _cachedMessages.value = updatedMessages
        
        return OfflineResult(
            success = true,
            isCached = true,
            message = "Message cached"
        )
    }
    
    override fun pullGachaOffline(userId: String, poolId: String, gachaManager: Any): OfflineResult {
        if (isOnline()) {
            return OfflineResult(
                success = false,
                isOffline = false,
                errorMessage = "Operation should be done online"
            )
        }
        
        return OfflineResult(
            success = false,
            isOffline = true,
            errorMessage = "Gacha not available offline"
        )
    }
    
    override fun triggerEventOffline(userId: String, eventId: String, eventManager: Any): OfflineResult {
        if (isOnline()) {
            return OfflineResult(
                success = false,
                isOffline = false,
                errorMessage = "Operation should be done online"
            )
        }
        
        return OfflineResult(
            success = false,
            isOffline = true,
            errorMessage = "Events not available offline"
        )
    }
    
    override fun updateCurrencyOffline(userId: String, currencyType: CurrencyType, amount: Int, currencyManager: Any): OfflineResult {
        if (isOnline()) {
            return OfflineResult(
                success = false,
                isOffline = false,
                errorMessage = "Operation should be done online"
            )
        }
        
        val updatedUpdates = _cachedCurrencyUpdates.value.toMutableMap()
        val userUpdates = updatedUpdates[userId]?.toMutableList() ?: mutableListOf()
        userUpdates.add(CurrencyUpdate(currencyType, amount))
        updatedUpdates[userId] = userUpdates
        _cachedCurrencyUpdates.value = updatedUpdates
        
        return OfflineResult(
            success = true,
            isCached = true,
            message = "Currency update cached"
        )
    }
    
    override fun syncData(userId: String, manager: Any): SyncResult {
        if (!isOnline()) {
            return SyncResult(
                success = false,
                syncedTasks = 0,
                errorMessage = "Cannot sync while offline"
            )
        }
        
        val syncedTasks = _cachedTaskCompletions.value[userId]?.size ?: 0
        
        val updatedCompletions = _cachedTaskCompletions.value.toMutableMap()
        updatedCompletions.remove(userId)
        _cachedTaskCompletions.value = updatedCompletions
        
        return SyncResult(
            success = true,
            syncedTasks = syncedTasks
        )
    }
    
    override fun cacheData(userId: String, data: Map<String, Any>) {
        val updatedData = _cachedData.value.toMutableMap()
        updatedData[userId] = data
        _cachedData.value = updatedData
    }
    
    override fun cacheDataWithExpiration(userId: String, data: Map<String, Any>, hours: Int) {
        val updatedData = _cachedData.value.toMutableMap()
        val userData = updatedData[userId]?.toMutableMap() ?: mutableMapOf()
        userData.putAll(data)
        updatedData[userId] = userData
        _cachedData.value = updatedData
    }
    
    override fun getCachedData(userId: String): Map<String, Any>? {
        return _cachedData.value[userId]
    }
    
    override fun getCachedTaskCompletions(userId: String): Set<String> {
        return _cachedTaskCompletions.value[userId] ?: emptySet()
    }
    
    override fun getCachedRewardClaims(userId: String): Set<String> {
        return _cachedRewardClaims.value[userId] ?: emptySet()
    }
    
    override fun getCachedMessages(userId: String): List<String> {
        return _cachedMessages.value[userId] ?: emptyList()
    }
    
    override fun getCachedCurrencyUpdates(userId: String): List<CurrencyUpdate> {
        return _cachedCurrencyUpdates.value[userId] ?: emptyList()
    }
    
    override fun getOfflineQueueSize(userId: String): Int {
        return (_cachedTaskCompletions.value[userId]?.size ?: 0) +
               (_cachedRewardClaims.value[userId]?.size ?: 0) +
               (_cachedMessages.value[userId]?.size ?: 0) +
               (_cachedCurrencyUpdates.value[userId]?.size ?: 0)
    }
    
    override fun performOfflineOperation(userId: String): OfflineResult {
        if (isOnline()) {
            return OfflineResult(
                success = false,
                isOffline = false,
                errorMessage = "Operation should be done offline"
            )
        }
        
        return OfflineResult(
            success = false,
            isOffline = true,
            errorMessage = "Operation failed offline"
        )
    }
}

interface OfflineManager {
    fun isOnline(): Boolean
    fun setOnline(isOnline: Boolean)
    fun isOffline(): Boolean
    fun signIn(userId: String): OfflineResult
    fun completeTaskOffline(userId: String, taskId: String, taskManager: Any): OfflineResult
    fun claimRewardOffline(userId: String, rewardId: String, rewardManager: Any): OfflineResult
    fun sendMessageOffline(userId: String, message: String, chatManager: Any): OfflineResult
    fun pullGachaOffline(userId: String, poolId: String, gachaManager: Any): OfflineResult
    fun triggerEventOffline(userId: String, eventId: String, eventManager: Any): OfflineResult
    fun updateCurrencyOffline(userId: String, currencyType: CurrencyType, amount: Int, currencyManager: Any): OfflineResult
    fun syncData(userId: String, manager: Any): SyncResult
    fun cacheData(userId: String, data: Map<String, Any>)
    fun cacheDataWithExpiration(userId: String, data: Map<String, Any>, hours: Int)
    fun getCachedData(userId: String): Map<String, Any>?
    fun getCachedTaskCompletions(userId: String): Set<String>
    fun getCachedRewardClaims(userId: String): Set<String>
    fun getCachedMessages(userId: String): List<String>
    fun getCachedCurrencyUpdates(userId: String): List<CurrencyUpdate>
    fun getOfflineQueueSize(userId: String): Int
    fun performOfflineOperation(userId: String): OfflineResult
}

data class OfflineResult(
    val success: Boolean,
    val isOffline: Boolean = false,
    val isCached: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

data class SyncResult(
    val success: Boolean,
    val syncedTasks: Int,
    val errorMessage: String? = null
)

data class CurrencyUpdate(
    val currencyType: CurrencyType,
    val amount: Int
)

class TestTaskManager
class TestRewardManager
class TestChatManager
class TestGachaManager
class TestEventManager
