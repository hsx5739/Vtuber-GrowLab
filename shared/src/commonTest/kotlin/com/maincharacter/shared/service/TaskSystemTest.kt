package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class TaskDefTest {
    
    @Test
    fun testCanComplete() {
        val taskDef = TaskDef(
            id = "task_001",
            name = "Test Task",
            description = "Test Description",
            type = TaskType.A_SELF_REPORT,
            category = TaskCategory.DAILY,
            maxCompletionsPerDay = 3
        )
        
        assertTrue(taskDef.canComplete(0))
        assertTrue(taskDef.canComplete(1))
        assertTrue(taskDef.canComplete(2))
        assertFalse(taskDef.canComplete(3))
    }
    
    @Test
    fun testGetEffectiveReward_WithoutVerification() {
        val baseReward = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 100),
            statDelta = mapOf(StatKey.FORTUNE to 10)
        )
        
        val taskDef = TaskDef(
            id = "task_001",
            name = "Test Task",
            description = "Test Description",
            type = TaskType.A_SELF_REPORT,
            category = TaskCategory.DAILY,
            baseReward = baseReward,
            verificationBonus = RewardBundle(
                currencies = mapOf(CurrencyId.STAR_DUST to 50),
                statDelta = mapOf(StatKey.FORTUNE to 5)
            )
        )
        
        val reward = taskDef.getEffectiveReward(false)
        assertEquals(100, reward.currencies[CurrencyId.STAR_DUST])
        assertEquals(10, reward.statDelta[StatKey.FORTUNE])
    }
    
    @Test
    fun testGetEffectiveReward_WithVerification() {
        val baseReward = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 100),
            statDelta = mapOf(StatKey.FORTUNE to 10)
        )
        
        val verificationBonus = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 50),
            statDelta = mapOf(StatKey.FORTUNE to 5)
        )
        
        val taskDef = TaskDef(
            id = "task_001",
            name = "Test Task",
            description = "Test Description",
            type = TaskType.A_SELF_REPORT,
            category = TaskCategory.DAILY,
            baseReward = baseReward,
            verificationBonus = verificationBonus
        )
        
        val reward = taskDef.getEffectiveReward(true)
        assertEquals(150, reward.currencies[CurrencyId.STAR_DUST])
        assertEquals(15, reward.statDelta[StatKey.FORTUNE])
    }
    
    @Test
    fun testGetDiminishedReward_NoDiminishing() {
        val baseReward = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 100)
        )
        
        val taskDef = TaskDef(
            id = "task_001",
            name = "Test Task",
            description = "Test Description",
            type = TaskType.A_SELF_REPORT,
            category = TaskCategory.DAILY,
            baseReward = baseReward,
            diminishingReturns = false
        )
        
        val reward = taskDef.getDiminishedReward(5)
        assertEquals(100, reward.currencies[CurrencyId.STAR_DUST])
    }
    
    @Test
    fun testGetDiminishedReward_WithDiminishing() {
        val baseReward = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 100)
        )
        
        val taskDef = TaskDef(
            id = "task_001",
            name = "Test Task",
            description = "Test Description",
            type = TaskType.A_SELF_REPORT,
            category = TaskCategory.DAILY,
            baseReward = baseReward,
            diminishingReturns = true
        )
        
        val reward1 = taskDef.getDiminishedReward(1)
        assertEquals(100, reward1.currencies[CurrencyId.STAR_DUST])
        
        val reward2 = taskDef.getDiminishedReward(2)
        assertEquals(66, reward2.currencies[CurrencyId.STAR_DUST])
        
        val reward3 = taskDef.getDiminishedReward(3)
        assertEquals(50, reward3.currencies[CurrencyId.STAR_DUST])
    }
    
    @Test
    fun testIsUnlockable_NoConditions() {
        val taskDef = TaskDef(
            id = "task_001",
            name = "Test Task",
            description = "Test Description",
            type = TaskType.A_SELF_REPORT,
            category = TaskCategory.DAILY,
            baseReward = RewardBundle()
        )
        
        val hostState = HostState(bond = 0)
        val flags = emptySet<String>()
        val inventory = emptySet<String>()
        
        assertTrue(taskDef.isUnlockable(hostState, flags, inventory))
    }
    
    @Test
    fun testIsUnlockable_WithBondRequirement() {
        val taskDef = TaskDef(
            id = "task_001",
            name = "Test Task",
            description = "Test Description",
            type = TaskType.A_SELF_REPORT,
            category = TaskCategory.DAILY,
            baseReward = RewardBundle(),
            minBond = 50
        )
        
        val hostState1 = HostState(bond = 30)
        val hostState2 = HostState(bond = 60)
        val flags = emptySet<String>()
        val inventory = emptySet<String>()
        
        assertFalse(taskDef.isUnlockable(hostState1, flags, inventory))
        assertTrue(taskDef.isUnlockable(hostState2, flags, inventory))
    }
    
    @Test
    fun testIsUnlockable_WithUnlockConditions() {
        val taskDef = TaskDef(
            id = "task_001",
            name = "Test Task",
            description = "Test Description",
            type = TaskType.A_SELF_REPORT,
            category = TaskCategory.DAILY,
            baseReward = RewardBundle(),
            unlockConditions = UnlockConditions(
                minBond = 30,
                requiredFlags = listOf("flag1", "flag2"),
                requiredItems = listOf("item1")
            )
        )
        
        val hostState = HostState(bond = 50)
        val flags = setOf("flag1", "flag2")
        val inventory = setOf("item1")
        
        assertTrue(taskDef.isUnlockable(hostState, flags, inventory))
        
        val flagsMissing = setOf("flag1")
        assertFalse(taskDef.isUnlockable(hostState, flagsMissing, inventory))
        
        val inventoryMissing = setOf("item2")
        assertFalse(taskDef.isUnlockable(hostState, flags, inventoryMissing))
    }
}

class TaskInstanceTest {
    
    @Test
    fun testCanStart() {
        val instance = TaskInstance(
            taskId = "task_001",
            state = TaskState.AVAILABLE
        )
        
        assertTrue(instance.canStart())
        
        val instance2 = instance.copy(state = TaskState.IN_PROGRESS)
        assertFalse(instance2.canStart())
    }
    
    @Test
    fun testCanComplete() {
        val progress = TaskProgress(current = 5, target = 10)
        val instance = TaskInstance(
            taskId = "task_001",
            state = TaskState.IN_PROGRESS,
            progress = progress
        )
        
        assertFalse(instance.canComplete())
        
        val completedProgress = TaskProgress(current = 10, target = 10)
        val instance2 = instance.copy(progress = completedProgress)
        assertTrue(instance2.canComplete())
    }
    
    @Test
    fun testCanVerify() {
        val instance = TaskInstance(
            taskId = "task_001",
            state = TaskState.COMPLETED,
            lastCompletionTime = System.currentTimeMillis() - 1000
        )
        
        assertTrue(instance.canVerify())
        
        val instance2 = instance.copy(state = TaskState.IN_PROGRESS)
        assertFalse(instance2.canVerify())
    }
    
    @Test
    fun testIsVerificationExpired() {
        val instance = TaskInstance(
            taskId = "task_001",
            state = TaskState.COMPLETED,
            lastCompletionTime = System.currentTimeMillis() - 1000
        )
        
        assertFalse(instance.isVerificationExpired())
        
        val expiredInstance = instance.copy(
            lastCompletionTime = System.currentTimeMillis() - 400000
        )
        assertTrue(expiredInstance.isVerificationExpired())
    }
    
    @Test
    fun testStart() {
        val instance = TaskInstance(
            taskId = "task_001",
            state = TaskState.AVAILABLE
        )
        
        val startedInstance = instance.start()
        assertEquals(TaskState.IN_PROGRESS, startedInstance.state)
        assertTrue(startedInstance.startTime > 0)
    }
    
    @Test
    fun testComplete() {
        val instance = TaskInstance(
            taskId = "task_001",
            state = TaskState.IN_PROGRESS
        )
        
        val completedInstance = instance.complete()
        assertEquals(TaskState.COMPLETED, completedInstance.state)
        assertEquals(1, completedInstance.completionsToday)
        assertTrue(completedInstance.lastCompletionTime > 0)
    }
    
    @Test
    fun testVerify() {
        val instance = TaskInstance(
            taskId = "task_001",
            state = TaskState.COMPLETED
        )
        
        val verifiedInstance = instance.verify()
        assertEquals(TaskState.COMPLETED_VERIFIED, verifiedInstance.state)
        assertTrue(verifiedInstance.lastVerificationTime > 0)
    }
    
    @Test
    fun testReset() {
        val instance = TaskInstance(
            taskId = "task_001",
            state = TaskState.COMPLETED,
            completionsToday = 3,
            startTime = 1000,
            endTime = 2000
        )
        
        val resetInstance = instance.reset()
        assertEquals(TaskState.AVAILABLE, resetInstance.state)
        assertEquals(0, resetInstance.completionsToday)
        assertEquals(0L, resetInstance.startTime)
        assertEquals(0L, resetInstance.endTime)
    }
    
    @Test
    fun testUpdateProgress() {
        val instance = TaskInstance(
            taskId = "task_001",
            state = TaskState.IN_PROGRESS
        )
        
        val newProgress = TaskProgress(current = 5, target = 10)
        val updatedInstance = instance.updateProgress(newProgress)
        
        assertEquals(5, updatedInstance.progress.current)
        assertEquals(10, updatedInstance.progress.target)
    }
}

class TaskProgressTest {
    
    @Test
    fun testIsComplete() {
        val progress = TaskProgress(current = 10, target = 10)
        assertTrue(progress.isComplete())
        
        val progress2 = TaskProgress(current = 5, target = 10)
        assertFalse(progress2.isComplete())
    }
    
    @Test
    fun testGetPercentage() {
        val progress = TaskProgress(current = 5, target = 10)
        assertEquals(50f, progress.getPercentage())
        
        val progress2 = TaskProgress(current = 0, target = 10)
        assertEquals(0f, progress2.getPercentage())
        
        val progress3 = TaskProgress(current = 10, target = 10)
        assertEquals(100f, progress3.getPercentage())
    }
    
    @Test
    fun testAdvance() {
        val progress = TaskProgress(current = 5, target = 10)
        val advanced = progress.advance(3)
        
        assertEquals(8, advanced.current)
        assertEquals(80f, advanced.percentage)
    }
    
    @Test
    fun testAdvance_ClampToTarget() {
        val progress = TaskProgress(current = 8, target = 10)
        val advanced = progress.advance(5)
        
        assertEquals(10, advanced.current)
        assertEquals(100f, advanced.percentage)
    }
    
    @Test
    fun testAdvance_WithZeroTarget() {
        val progress = TaskProgress(current = 0, target = 0)
        val percentage = progress.getPercentage()
        
        assertEquals(100f, percentage)
    }
}

class TaskConfigTest {
    
    @Test
    fun testTaskConfigDefaults() {
        val config = TaskConfig()
        
        assertEquals(null, config.durationSec)
        assertEquals(null, config.targetCount)
        assertEquals(null, config.requiredItems)
        assertEquals(null, config.dialogueId)
        assertEquals(null, config.eventId)
        assertFalse(config.imageRecognition)
        assertFalse(config.voiceDetection)
    }
    
    @Test
    fun testTaskConfigWithDuration() {
        val config = TaskConfig(durationSec = 3600)
        
        assertEquals(3600, config.durationSec)
    }
    
    @Test
    fun testTaskConfigWithImageRecognition() {
        val config = TaskConfig(imageRecognition = true)
        
        assertTrue(config.imageRecognition)
    }
}

class TaskConstraintsTest {
    
    @Test
    fun testTaskConstraintsDefaults() {
        val constraints = TaskConstraints()
        
        assertEquals(null, constraints.maxAttempts)
        assertEquals(null, constraints.timeLimitSec)
        assertEquals(0, constraints.cooldownSec)
        assertTrue(constraints.requiredItems.isEmpty())
    }
    
    @Test
    fun testTaskConstraintsWithCooldown() {
        val constraints = TaskConstraints(cooldownSec = 300)
        
        assertEquals(300, constraints.cooldownSec)
    }
    
    @Test
    fun testTaskConstraintsWithMaxAttempts() {
        val constraints = TaskConstraints(maxAttempts = 3)
        
        assertEquals(3, constraints.maxAttempts)
    }
}

class UnlockConditionsTest {
    
    @Test
    fun testUnlockConditionsDefaults() {
        val conditions = UnlockConditions()
        
        assertEquals(0, conditions.minBond)
        assertTrue(conditions.requiredFlags.isEmpty())
        assertTrue(conditions.requiredItems.isEmpty())
        assertEquals(0, conditions.minLevel)
    }
    
    @Test
    fun testUnlockConditionsWithBond() {
        val conditions = UnlockConditions(minBond = 50)
        
        assertEquals(50, conditions.minBond)
    }
    
    @Test
    fun testUnlockConditionsWithFlags() {
        val conditions = UnlockConditions(requiredFlags = listOf("flag1", "flag2"))
        
        assertEquals(2, conditions.requiredFlags.size)
        assertTrue(conditions.requiredFlags.contains("flag1"))
        assertTrue(conditions.requiredFlags.contains("flag2"))
    }
}

class TaskEnumsTest {
    
    @Test
    fun testTaskTypeValues() {
        val types = TaskType.values()
        assertEquals(8, types.size)
        assertTrue(types.contains(TaskType.A_SELF_REPORT))
        assertTrue(types.contains(TaskType.B_TIMER))
        assertTrue(types.contains(TaskType.C_HEALTH_DATA))
        assertTrue(types.contains(TaskType.D_DIALOGUE))
        assertTrue(types.contains(TaskType.F_IMAGE_RECOGNITION))
        assertTrue(types.contains(TaskType.G_VOICE_DETECTION))
        assertTrue(types.contains(TaskType.H_EVENT))
    }
    
    @Test
    fun testTaskCategoryValues() {
        val categories = TaskCategory.values()
        assertEquals(5, categories.size)
        assertTrue(categories.contains(TaskCategory.DAILY))
        assertTrue(categories.contains(TaskCategory.WEEKLY))
        assertTrue(categories.contains(TaskCategory.ONE_TIME))
        assertTrue(categories.contains(TaskCategory.EVENT))
        assertTrue(categories.contains(TaskCategory.TUTORIAL))
    }
    
    @Test
    fun testTaskPriorityValues() {
        val priorities = TaskPriority.values()
        assertEquals(4, priorities.size)
        assertTrue(priorities.contains(TaskPriority.LOW))
        assertTrue(priorities.contains(TaskPriority.NORMAL))
        assertTrue(priorities.contains(TaskPriority.HIGH))
        assertTrue(priorities.contains(TaskPriority.URGENT))
    }
    
    @Test
    fun testTaskDifficultyValues() {
        val difficulties = TaskDifficulty.values()
        assertEquals(4, difficulties.size)
        assertTrue(difficulties.contains(TaskDifficulty.EASY))
        assertTrue(difficulties.contains(TaskDifficulty.NORMAL))
        assertTrue(difficulties.contains(TaskDifficulty.HARD))
        assertTrue(difficulties.contains(TaskDifficulty.EXPERT))
    }
    
    @Test
    fun testTaskStateValues() {
        val states = TaskState.values()
        assertEquals(7, states.size)
        assertTrue(states.contains(TaskState.LOCKED))
        assertTrue(states.contains(TaskState.AVAILABLE))
        assertTrue(states.contains(TaskState.IN_PROGRESS))
        assertTrue(states.contains(TaskState.COMPLETED))
        assertTrue(states.contains(TaskState.COMPLETED_VERIFIED))
        assertTrue(states.contains(TaskState.EXPIRED))
        assertTrue(states.contains(TaskState.CANCELLED))
    }
}