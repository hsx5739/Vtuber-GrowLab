package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BoundaryTriggerTest {
    
    @Test
    fun testDailyBoundaryTrigger() {
        val boundaryManager = TestBoundaryManager()
        val userId = "user_boundary_001"
        
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        boundaryManager.setLastSignInTime(userId, yesterday)
        
        val isTriggered = boundaryManager.checkDailyBoundary(userId)
        assertTrue(isTriggered)
        
        val newSignInTime = System.currentTimeMillis()
        boundaryManager.setLastSignInTime(userId, newSignInTime)
        
        val isTriggeredAgain = boundaryManager.checkDailyBoundary(userId)
        assertFalse(isTriggeredAgain)
    }
    
    @Test
    fun testWeeklyBoundaryTrigger() {
        val boundaryManager = TestBoundaryManager()
        val userId = "user_boundary_002"
        
        val lastWeek = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        boundaryManager.setLastWeeklyResetTime(userId, lastWeek)
        
        val isTriggered = boundaryManager.checkWeeklyBoundary(userId)
        assertTrue(isTriggered)
        
        val newResetTime = System.currentTimeMillis()
        boundaryManager.setLastWeeklyResetTime(userId, newResetTime)
        
        val isTriggeredAgain = boundaryManager.checkWeeklyBoundary(userId)
        assertFalse(isTriggeredAgain)
    }
    
    @Test
    fun testDailyTaskReset() {
        val boundaryManager = TestBoundaryManager()
        val taskManager = TestTaskManager()
        val userId = "user_boundary_003"
        
        val taskId = "task_daily_001"
        taskManager.createTaskInstance(userId, taskId, TaskType.DAILY)
        
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        boundaryManager.setLastDailyResetTime(userId, yesterday)
        
        boundaryManager.triggerDailyReset(userId, taskManager)
        
        val taskInstance = taskManager.getTaskInstance(userId, taskId)
        assertNotNull(taskInstance)
        assertEquals(TaskState.AVAILABLE, taskInstance.state)
    }
    
    @Test
    fun testWeeklyTaskReset() {
        val boundaryManager = TestBoundaryManager()
        val taskManager = TestTaskManager()
        val userId = "user_boundary_004"
        
        val taskId = "task_weekly_001"
        taskManager.createTaskInstance(userId, taskId, TaskType.WEEKLY)
        
        val lastWeek = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        boundaryManager.setLastWeeklyResetTime(userId, lastWeek)
        
        boundaryManager.triggerWeeklyReset(userId, taskManager)
        
        val taskInstance = taskManager.getTaskInstance(userId, taskId)
        assertNotNull(taskInstance)
        assertEquals(TaskState.AVAILABLE, taskInstance.state)
    }
    
    @Test
    fun testStreakResetOnDailyBoundary() {
        val boundaryManager = TestBoundaryManager()
        val signInManager = TestSignInManager()
        val userId = "user_boundary_005"
        
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        signInManager.setLastSignInTime(userId, yesterday)
        signInManager.setStreak(userId, 5)
        
        boundaryManager.triggerDailyReset(userId, signInManager)
        
        val streak = signInManager.getStreak(userId)
        assertEquals(0, streak)
    }
    
    @Test
    fun testSignInStreakNotResetWithinDay() {
        val boundaryManager = TestBoundaryManager()
        val signInManager = TestSignInManager()
        val userId = "user_boundary_006"
        
        val today = System.currentTimeMillis() - 12 * 60 * 60 * 1000
        signInManager.setLastSignInTime(userId, today)
        signInManager.setStreak(userId, 5)
        
        boundaryManager.triggerDailyReset(userId, signInManager)
        
        val streak = signInManager.getStreak(userId)
        assertEquals(5, streak)
    }
    
    @Test
    fun testDailyRewardReset() {
        val boundaryManager = TestBoundaryManager()
        val rewardManager = TestRewardManager()
        val userId = "user_boundary_007"
        
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        boundaryManager.setLastDailyResetTime(userId, yesterday)
        
        boundaryManager.triggerDailyReset(userId, rewardManager)
        
        val dailyRewardClaimed = rewardManager.hasClaimedDailyReward(userId)
        assertFalse(dailyRewardClaimed)
    }
    
    @Test
    fun testWeeklyRewardReset() {
        val boundaryManager = TestBoundaryManager()
        val rewardManager = TestRewardManager()
        val userId = "user_boundary_008"
        
        val lastWeek = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        boundaryManager.setLastWeeklyResetTime(userId, lastWeek)
        
        boundaryManager.triggerWeeklyReset(userId, rewardManager)
        
        val weeklyRewardClaimed = rewardManager.hasClaimedWeeklyReward(userId)
        assertFalse(weeklyRewardClaimed)
    }
    
    @Test
    fun testEventCooldownReset() {
        val boundaryManager = TestBoundaryManager()
        val eventManager = TestEventManager()
        val userId = "user_boundary_009"
        
        val eventId = "evt_sandstorm_lite"
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        eventManager.setEventCooldown(userId, eventId, yesterday)
        
        boundaryManager.triggerDailyReset(userId, eventManager)
        
        val cooldownStatus = eventManager.getEventCooldownStatus(userId, eventId)
        assertFalse(cooldownStatus.isOnCooldown)
    }
    
    @Test
    fun testBoundaryTimeCalculation() {
        val boundaryManager = TestBoundaryManager()
        
        val currentTime = System.currentTimeMillis()
        val dailyBoundary = boundaryManager.calculateDailyBoundary(currentTime)
        val weeklyBoundary = boundaryManager.calculateWeeklyBoundary(currentTime)
        
        val expectedDailyBoundary = (currentTime / (24 * 60 * 60 * 1000)) * (24 * 60 * 60 * 1000)
        val expectedWeeklyBoundary = (currentTime / (7 * 24 * 60 * 60 * 1000)) * (7 * 24 * 60 * 60 * 1000)
        
        assertEquals(expectedDailyBoundary, dailyBoundary)
        assertEquals(expectedWeeklyBoundary, weeklyBoundary)
    }
    
    @Test
    fun testMultipleUserBoundaryHandling() {
        val boundaryManager = TestBoundaryManager()
        val userId1 = "user_boundary_010"
        val userId2 = "user_boundary_011"
        
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        boundaryManager.setLastSignInTime(userId1, yesterday)
        boundaryManager.setLastSignInTime(userId2, System.currentTimeMillis())
        
        val isTriggered1 = boundaryManager.checkDailyBoundary(userId1)
        val isTriggered2 = boundaryManager.checkDailyBoundary(userId2)
        
        assertTrue(isTriggered1)
        assertFalse(isTriggered2)
    }
    
    @Test
    fun testBoundaryNotification() {
        val boundaryManager = TestBoundaryManager()
        val userId = "user_boundary_012"
        
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        boundaryManager.setLastSignInTime(userId, yesterday)
        
        val notifications = boundaryManager.checkBoundaryAndNotify(userId)
        assertTrue(notifications.isNotEmpty())
        
        val dailyNotification = notifications.find { it.type == BoundaryNotificationType.DAILY }
        assertNotNull(dailyNotification)
    }
}

class TestBoundaryManager : BoundaryManager {
    private val _lastSignInTimes = MutableStateFlow<Map<String, Long>>(emptyMap())
    private val _lastDailyResetTimes = MutableStateFlow<Map<String, Long>>(emptyMap())
    private val _lastWeeklyResetTimes = MutableStateFlow<Map<String, Long>>(emptyMap())
    
    override fun checkDailyBoundary(userId: String): Boolean {
        val lastSignIn = _lastSignInTimes.value[userId] ?: return true
        val currentTime = System.currentTimeMillis()
        val dailyBoundary = calculateDailyBoundary(currentTime)
        return lastSignIn < dailyBoundary
    }
    
    override fun checkWeeklyBoundary(userId: String): Boolean {
        val lastReset = _lastWeeklyResetTimes.value[userId] ?: return true
        val currentTime = System.currentTimeMillis()
        val weeklyBoundary = calculateWeeklyBoundary(currentTime)
        return lastReset < weeklyBoundary
    }
    
    override fun triggerDailyReset(userId: String, manager: Any) {
        val currentTime = System.currentTimeMillis()
        val updatedResets = _lastDailyResetTimes.value.toMutableMap()
        updatedResets[userId] = currentTime
        _lastDailyResetTimes.value = updatedResets
        
        when (manager) {
            is TestTaskManager -> manager.resetDailyTasks(userId)
            is TestSignInManager -> manager.resetDailyStreak(userId)
            is TestRewardManager -> manager.resetDailyRewards(userId)
            is TestEventManager -> manager.resetDailyEventCooldowns(userId)
        }
    }
    
    override fun triggerWeeklyReset(userId: String, manager: Any) {
        val currentTime = System.currentTimeMillis()
        val updatedResets = _lastWeeklyResetTimes.value.toMutableMap()
        updatedResets[userId] = currentTime
        _lastWeeklyResetTimes.value = updatedResets
        
        when (manager) {
            is TestTaskManager -> manager.resetWeeklyTasks(userId)
            is TestRewardManager -> manager.resetWeeklyRewards(userId)
        }
    }
    
    override fun setLastSignInTime(userId: String, time: Long) {
        val updatedTimes = _lastSignInTimes.value.toMutableMap()
        updatedTimes[userId] = time
        _lastSignInTimes.value = updatedTimes
    }
    
    override fun setLastDailyResetTime(userId: String, time: Long) {
        val updatedTimes = _lastDailyResetTimes.value.toMutableMap()
        updatedTimes[userId] = time
        _lastDailyResetTimes.value = updatedTimes
    }
    
    override fun setLastWeeklyResetTime(userId: String, time: Long) {
        val updatedTimes = _lastWeeklyResetTimes.value.toMutableMap()
        updatedTimes[userId] = time
        _lastWeeklyResetTimes.value = updatedTimes
    }
    
    override fun calculateDailyBoundary(currentTime: Long): Long {
        return (currentTime / (24 * 60 * 60 * 1000)) * (24 * 60 * 60 * 1000)
    }
    
    override fun calculateWeeklyBoundary(currentTime: Long): Long {
        return (currentTime / (7 * 24 * 60 * 60 * 1000)) * (7 * 24 * 60 * 60 * 1000)
    }
    
    override fun checkBoundaryAndNotify(userId: String): List<BoundaryNotification> {
        val notifications = mutableListOf<BoundaryNotification>()
        
        if (checkDailyBoundary(userId)) {
            notifications.add(BoundaryNotification(
                type = BoundaryNotificationType.DAILY,
                message = "新的一天开始了！"
            ))
        }
        
        if (checkWeeklyBoundary(userId)) {
            notifications.add(BoundaryNotification(
                type = BoundaryNotificationType.WEEKLY,
                message = "新的一周开始了！"
            ))
        }
        
        return notifications
    }
}

interface BoundaryManager {
    fun checkDailyBoundary(userId: String): Boolean
    fun checkWeeklyBoundary(userId: String): Boolean
    fun triggerDailyReset(userId: String, manager: Any)
    fun triggerWeeklyReset(userId: String, manager: Any)
    fun setLastSignInTime(userId: String, time: Long)
    fun setLastDailyResetTime(userId: String, time: Long)
    fun setLastWeeklyResetTime(userId: String, time: Long)
    fun calculateDailyBoundary(currentTime: Long): Long
    fun calculateWeeklyBoundary(currentTime: Long): Long
    fun checkBoundaryAndNotify(userId: String): List<BoundaryNotification>
}

enum class BoundaryNotificationType {
    DAILY,
    WEEKLY
}

data class BoundaryNotification(
    val type: BoundaryNotificationType,
    val message: String
)

class TestTaskManager {
    private val _taskInstances = MutableStateFlow<Map<String, TaskInstance>>(emptyMap())
    
    fun createTaskInstance(userId: String, taskId: String, type: TaskType) {
        val instance = TaskInstance(
            id = "${userId}_${taskId}",
            taskId = taskId,
            userId = userId,
            type = type,
            state = TaskState.IN_PROGRESS,
            progress = 0,
            startTime = System.currentTimeMillis()
        )
        val updatedInstances = _taskInstances.value.toMutableMap()
        updatedInstances[instance.id] = instance
        _taskInstances.value = updatedInstances
    }
    
    fun getTaskInstance(userId: String, taskId: String): TaskInstance? {
        return _taskInstances.values.find { it.userId == userId && it.taskId == taskId }
    }
    
    fun resetDailyTasks(userId: String) {
        val instances = _taskInstances.value.filter { it.userId == userId && it.type == TaskType.DAILY }
        val updatedInstances = _taskInstances.value.toMutableMap()
        instances.forEach { (id, instance) ->
            updatedInstances[id] = instance.copy(state = TaskState.AVAILABLE, progress = 0)
        }
        _taskInstances.value = updatedInstances
    }
    
    fun resetWeeklyTasks(userId: String) {
        val instances = _taskInstances.value.filter { it.userId == userId && it.type == TaskType.WEEKLY }
        val updatedInstances = _taskInstances.value.toMutableMap()
        instances.forEach { (id, instance) ->
            updatedInstances[id] = instance.copy(state = TaskState.AVAILABLE, progress = 0)
        }
        _taskInstances.value = updatedInstances
    }
}

class TestSignInManager {
    private val _streaks = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _lastSignInTimes = MutableStateFlow<Map<String, Long>>(emptyMap())
    
    fun setStreak(userId: String, streak: Int) {
        val updatedStreaks = _streaks.value.toMutableMap()
        updatedStreaks[userId] = streak
        _streaks.value = updatedStreaks
    }
    
    fun setLastSignInTime(userId: String, time: Long) {
        val updatedTimes = _lastSignInTimes.value.toMutableMap()
        updatedTimes[userId] = time
        _lastSignInTimes.value = updatedTimes
    }
    
    fun getStreak(userId: String): Int {
        return _streaks.value[userId] ?: 0
    }
    
    fun resetDailyStreak(userId: String) {
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        val lastSignIn = _lastSignInTimes.value[userId] ?: return
        
        if (lastSignIn < yesterday) {
            val updatedStreaks = _streaks.value.toMutableMap()
            updatedStreaks[userId] = 0
            _streaks.value = updatedStreaks
        }
    }
}

class TestRewardManager {
    private val _dailyRewardClaimed = MutableStateFlow<Set<String>>(emptySet())
    private val _weeklyRewardClaimed = MutableStateFlow<Set<String>>(emptySet())
    
    fun hasClaimedDailyReward(userId: String): Boolean {
        return userId in _dailyRewardClaimed.value
    }
    
    fun hasClaimedWeeklyReward(userId: String): Boolean {
        return userId in _weeklyRewardClaimed.value
    }
    
    fun resetDailyRewards(userId: String) {
        val updatedClaimed = _dailyRewardClaimed.value.toMutableSet()
        updatedClaimed.remove(userId)
        _dailyRewardClaimed.value = updatedClaimed
    }
    
    fun resetWeeklyRewards(userId: String) {
        val updatedClaimed = _weeklyRewardClaimed.value.toMutableSet()
        updatedClaimed.remove(userId)
        _weeklyRewardClaimed.value = updatedClaimed
    }
}

class TestEventManager {
    private val _eventCooldowns = MutableStateFlow<Map<String, Map<String, Long>>>(emptyMap())
    
    fun setEventCooldown(userId: String, eventId: String, time: Long) {
        val updatedCooldowns = _eventCooldowns.value.toMutableMap()
        val userCooldowns = updatedCooldowns[userId]?.toMutableMap() ?: mutableMapOf()
        userCooldowns[eventId] = time
        updatedCooldowns[userId] = userCooldowns
        _eventCooldowns.value = updatedCooldowns
    }
    
    fun getEventCooldownStatus(userId: String, eventId: String): EventCooldownStatus {
        val userCooldowns = _eventCooldowns.value[userId] ?: return EventCooldownStatus(isOnCooldown = false)
        val lastCooldown = userCooldowns[eventId] ?: return EventCooldownStatus(isOnCooldown = false)
        
        val currentTime = System.currentTimeMillis()
        val isOnCooldown = (currentTime - lastCooldown) < 24 * 60 * 60 * 1000
        
        return EventCooldownStatus(isOnCooldown = isOnCooldown)
    }
    
    fun resetDailyEventCooldowns(userId: String) {
        val updatedCooldowns = _eventCooldowns.value.toMutableMap()
        updatedCooldowns.remove(userId)
        _eventCooldowns.value = updatedCooldowns
    }
}

data class EventCooldownStatus(
    val isOnCooldown: Boolean
)
