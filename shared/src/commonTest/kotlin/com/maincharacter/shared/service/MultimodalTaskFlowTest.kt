package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MultimodalTaskFlowTest {

    @Test
    fun testBClassTimerTaskFlow() {
        val userId = "user_timer_001"
        val taskSystem = TaskSystem()
        val currencyLedger = CurrencyLedger()
        val attributeCalculator = AttributeCalculator()
        val rewardDispatcher = RewardDispatcher(currencyLedger, attributeCalculator)

        val timerTask = TaskDef(
            taskId = "task_focus_25",
            taskName = "专注25分钟",
            taskDescription = "集中注意力完成一件小事",
            taskType = TaskType.B,
            taskCategory = TaskCategory.GAMEPLAY,
            requirements = TaskRequirements(
                targetValue = 1500,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 15),
                statDelta = mapOf("vitality" to 5),
                items = emptyList(),
                buffs = emptyList()
            ),
            verifiedRewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 30),
                statDelta = mapOf("vitality" to 10, "focus" to 5),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        taskSystem.registerTask(timerTask)

        val initialProgress = taskSystem.getProgress(userId, timerTask.taskId)
        assertNotNull(initialProgress)
        assertEquals(0, initialProgress.currentProgress)

        val timerDuration = 1500L
        taskSystem.updateProgress(userId, timerTask.taskId, timerDuration.toInt())

        val afterTimerProgress = taskSystem.getProgress(userId, timerTask.taskId)
        assertNotNull(afterTimerProgress)
        assertEquals(1500, afterTimerProgress.currentProgress)
        assertTrue(afterTimerProgress.isCompleted)

        val baseReward = taskSystem.claimReward(userId, timerTask.taskId)
        assertNotNull(baseReward)
        assertEquals(15, baseReward.currencies[CurrencyType.STAR_DUST])

        rewardDispatcher.dispatchRewards(userId, baseReward)

        val verifiedReward = taskSystem.claimVerifiedReward(userId, timerTask.taskId)
        assertNotNull(verifiedReward)
        assertEquals(30, verifiedReward.currencies[CurrencyType.STAR_DUST])

        rewardDispatcher.dispatchRewards(userId, verifiedReward)

        assertEquals(45, currencyLedger.getBalance(userId, CurrencyType.STAR_DUST))
    }

    @Test
    fun testBClassTimerTaskWithPause() {
        val userId = "user_timer_002"
        val taskSystem = TaskSystem()

        val timerTask = TaskDef(
            taskId = "task_focus_60",
            taskName = "专注60分钟",
            taskDescription = "长时间专注工作",
            taskType = TaskType.B,
            taskCategory = TaskCategory.GAMEPLAY,
            requirements = TaskRequirements(
                targetValue = 3600,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 30),
                statDelta = mapOf("vitality" to 10),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        taskSystem.registerTask(timerTask)

        taskSystem.updateProgress(userId, timerTask.taskId, 1800)
        var progress = taskSystem.getProgress(userId, timerTask.taskId)
        assertNotNull(progress)
        assertEquals(1800, progress.currentProgress)

        taskSystem.updateProgress(userId, timerTask.taskId, 900)
        progress = taskSystem.getProgress(userId, timerTask.taskId)
        assertNotNull(progress)
        assertEquals(2700, progress.currentProgress)

        taskSystem.updateProgress(userId, timerTask.taskId, 900)
        progress = taskSystem.getProgress(userId, timerTask.taskId)
        assertNotNull(progress)
        assertEquals(3600, progress.currentProgress)
        assertTrue(progress.isCompleted)
    }

    @Test
    fun testCClassHealthDataTaskFlow() {
        val userId = "user_health_001"
        val taskSystem = TaskSystem()
        val currencyLedger = CurrencyLedger()
        val attributeCalculator = AttributeCalculator()
        val rewardDispatcher = RewardDispatcher(currencyLedger, attributeCalculator)

        val stepsTask = TaskDef(
            taskId = "task_daily_steps",
            taskName = "今日步数达标",
            taskDescription = "走路6000步，让身体动起来",
            taskType = TaskType.C,
            taskCategory = TaskCategory.HEALTH,
            requirements = TaskRequirements(
                targetValue = 6000,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 20),
                statDelta = mapOf("vitality" to 8),
                items = emptyList(),
                buffs = emptyList()
            ),
            verifiedRewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 40),
                statDelta = mapOf("vitality" to 15),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        taskSystem.registerTask(stepsTask)

        val authorizedSteps = 6500
        taskSystem.updateProgress(userId, stepsTask.taskId, authorizedSteps)

        val progress = taskSystem.getProgress(userId, stepsTask.taskId)
        assertNotNull(progress)
        assertEquals(6500, progress.currentProgress)
        assertTrue(progress.isCompleted)

        val verifiedReward = taskSystem.claimVerifiedReward(userId, stepsTask.taskId)
        assertNotNull(verifiedReward)
        assertEquals(40, verifiedReward.currencies[CurrencyType.STAR_DUST])

        rewardDispatcher.dispatchRewards(userId, verifiedReward)
        assertEquals(40, currencyLedger.getBalance(userId, CurrencyType.STAR_DUST))
    }

    @Test
    fun testCClassHealthDataUnauthorizedFlow() {
        val userId = "user_health_002"
        val taskSystem = TaskSystem()
        val currencyLedger = CurrencyLedger()
        val attributeCalculator = AttributeCalculator()
        val rewardDispatcher = RewardDispatcher(currencyLedger, attributeCalculator)

        val sleepTask = TaskDef(
            taskId = "task_sleep_7h",
            taskName = "睡眠7小时",
            taskDescription = "保证充足睡眠",
            taskType = TaskType.C,
            taskCategory = TaskCategory.HEALTH,
            requirements = TaskRequirements(
                targetValue = 420,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 25),
                statDelta = mapOf("vitality" to 12),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        taskSystem.registerTask(sleepTask)

        val selfReportedMinutes = 450
        taskSystem.updateProgress(userId, sleepTask.taskId, selfReportedMinutes)

        val progress = taskSystem.getProgress(userId, sleepTask.taskId)
        assertNotNull(progress)
        assertEquals(450, progress.currentProgress)
        assertTrue(progress.isCompleted)

        val baseReward = taskSystem.claimReward(userId, sleepTask.taskId)
        assertNotNull(baseReward)
        assertEquals(25, baseReward.currencies[CurrencyType.STAR_DUST])

        rewardDispatcher.dispatchRewards(userId, baseReward)
        assertEquals(25, currencyLedger.getBalance(userId, CurrencyType.STAR_DUST))
    }

    @Test
    fun testFClassPhotoTaskFlow() {
        val userId = "user_photo_001"
        val taskSystem = TaskSystem()
        val currencyLedger = CurrencyLedger()
        val attributeCalculator = AttributeCalculator()
        val rewardDispatcher = RewardDispatcher(currencyLedger, attributeCalculator)

        val photoTask = TaskDef(
            taskId = "task_photo_nature",
            taskName = "拍摄自然风景",
            taskDescription = "拍一张自然风景照片",
            taskType = TaskType.F,
            taskCategory = TaskCategory.GAMEPLAY,
            requirements = TaskRequirements(
                targetValue = 1,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 20),
                statDelta = mapOf("mood" to 8),
                items = emptyList(),
                buffs = emptyList()
            ),
            verifiedRewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 40),
                statDelta = mapOf("mood" to 15, "fortune" to 5),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        taskSystem.registerTask(photoTask)

        taskSystem.updateProgress(userId, photoTask.taskId, 1)

        val progress = taskSystem.getProgress(userId, photoTask.taskId)
        assertNotNull(progress)
        assertEquals(1, progress.currentProgress)
        assertTrue(progress.isCompleted)

        val baseReward = taskSystem.claimReward(userId, photoTask.taskId)
        assertNotNull(baseReward)
        assertEquals(20, baseReward.currencies[CurrencyType.STAR_DUST])

        rewardDispatcher.dispatchRewards(userId, baseReward)

        val verifiedReward = taskSystem.claimVerifiedReward(userId, photoTask.taskId)
        assertNotNull(verifiedReward)
        assertEquals(40, verifiedReward.currencies[CurrencyType.STAR_DUST])

        rewardDispatcher.dispatchRewards(userId, verifiedReward)

        assertEquals(60, currencyLedger.getBalance(userId, CurrencyType.STAR_DUST))
    }

    @Test
    fun testFClassPhotoTaskWithoutUpload() {
        val userId = "user_photo_002"
        val taskSystem = TaskSystem()
        val currencyLedger = CurrencyLedger()
        val attributeCalculator = AttributeCalculator()
        val rewardDispatcher = RewardDispatcher(currencyLedger, attributeCalculator)

        val photoTask = TaskDef(
            taskId = "task_photo_sky",
            taskName = "拍摄天空",
            taskDescription = "拍一张天空的照片",
            taskType = TaskType.F,
            taskCategory = TaskCategory.GAMEPLAY,
            requirements = TaskRequirements(
                targetValue = 1,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 15),
                statDelta = mapOf("mood" to 6),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        taskSystem.registerTask(photoTask)

        taskSystem.updateProgress(userId, photoTask.taskId, 1)

        val progress = taskSystem.getProgress(userId, photoTask.taskId)
        assertNotNull(progress)
        assertEquals(1, progress.currentProgress)
        assertTrue(progress.isCompleted)

        val baseReward = taskSystem.claimReward(userId, photoTask.taskId)
        assertNotNull(baseReward)
        assertEquals(15, baseReward.currencies[CurrencyType.STAR_DUST])

        rewardDispatcher.dispatchRewards(userId, baseReward)
        assertEquals(15, currencyLedger.getBalance(userId, CurrencyType.STAR_DUST))
    }

    @Test
    fun testGClassVoiceTaskFlow() {
        val userId = "user_voice_001"
        val taskSystem = TaskSystem()
        val currencyLedger = CurrencyLedger()
        val attributeCalculator = AttributeCalculator()
        val rewardDispatcher = RewardDispatcher(currencyLedger, attributeCalculator)

        val voiceTask = TaskDef(
            taskId = "task_voice_sing",
            taskName = "唱首歌",
            taskDescription = "唱一首你喜欢的歌",
            taskType = TaskType.G,
            taskCategory = TaskCategory.SOCIAL,
            requirements = TaskRequirements(
                targetValue = 30,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 20),
                statDelta = mapOf("mood" to 10),
                items = emptyList(),
                buffs = emptyList()
            ),
            verifiedRewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 40),
                statDelta = mapOf("mood" to 18, "bond" to 5),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        taskSystem.registerTask(voiceTask)

        val recordedSeconds = 35
        taskSystem.updateProgress(userId, voiceTask.taskId, recordedSeconds)

        val progress = taskSystem.getProgress(userId, voiceTask.taskId)
        assertNotNull(progress)
        assertEquals(35, progress.currentProgress)
        assertTrue(progress.isCompleted)

        val verifiedReward = taskSystem.claimVerifiedReward(userId, voiceTask.taskId)
        assertNotNull(verifiedReward)
        assertEquals(40, verifiedReward.currencies[CurrencyType.STAR_DUST])

        rewardDispatcher.dispatchRewards(userId, verifiedReward)
        assertEquals(40, currencyLedger.getBalance(userId, CurrencyType.STAR_DUST))
    }

    @Test
    fun testGClassVoiceTaskLowCompletionPath() {
        val userId = "user_voice_002"
        val taskSystem = TaskSystem()
        val currencyLedger = CurrencyLedger()
        val attributeCalculator = AttributeCalculator()
        val rewardDispatcher = RewardDispatcher(currencyLedger, attributeCalculator)

        val voiceTask = TaskDef(
            taskId = "task_voice_talk",
            taskName = "说一段话",
            taskDescription = "说一段话表达你的想法",
            taskType = TaskType.G,
            taskCategory = TaskCategory.SOCIAL,
            requirements = TaskRequirements(
                targetValue = 15,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 15),
                statDelta = mapOf("mood" to 7),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        taskSystem.registerTask(voiceTask)

        val selfReportedSeconds = 20
        taskSystem.updateProgress(userId, voiceTask.taskId, selfReportedSeconds)

        val progress = taskSystem.getProgress(userId, voiceTask.taskId)
        assertNotNull(progress)
        assertEquals(20, progress.currentProgress)
        assertTrue(progress.isCompleted)

        val baseReward = taskSystem.claimReward(userId, voiceTask.taskId)
        assertNotNull(baseReward)
        assertEquals(15, baseReward.currencies[CurrencyType.STAR_DUST])

        rewardDispatcher.dispatchRewards(userId, baseReward)
        assertEquals(15, currencyLedger.getBalance(userId, CurrencyType.STAR_DUST))
    }

    @Test
    fun testMultimodalTaskCombination() {
        val userId = "user_multi_001"
        val taskSystem = TaskSystem()
        val currencyLedger = CurrencyLedger()
        val attributeCalculator = AttributeCalculator()
        val rewardDispatcher = RewardDispatcher(currencyLedger, attributeCalculator)

        val timerTask = TaskDef(
            taskId = "task_focus_15",
            taskName = "专注15分钟",
            taskDescription = "集中注意力",
            taskType = TaskType.B,
            taskCategory = TaskCategory.GAMEPLAY,
            requirements = TaskRequirements(
                targetValue = 900,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 10),
                statDelta = mapOf("focus" to 5),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        val stepsTask = TaskDef(
            taskId = "task_steps_3000",
            taskName = "走3000步",
            taskDescription = "适量运动",
            taskType = TaskType.C,
            taskCategory = TaskCategory.HEALTH,
            requirements = TaskRequirements(
                targetValue = 3000,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 15),
                statDelta = mapOf("vitality" to 6),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        val voiceTask = TaskDef(
            taskId = "task_voice_20s",
            taskName = "说20秒话",
            taskDescription = "表达自己",
            taskType = TaskType.G,
            taskCategory = TaskCategory.SOCIAL,
            requirements = TaskRequirements(
                targetValue = 20,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 12),
                statDelta = mapOf("mood" to 5),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        taskSystem.registerTask(timerTask)
        taskSystem.registerTask(stepsTask)
        taskSystem.registerTask(voiceTask)

        taskSystem.updateProgress(userId, timerTask.taskId, 900)
        taskSystem.updateProgress(userId, stepsTask.taskId, 3500)
        taskSystem.updateProgress(userId, voiceTask.taskId, 25)

        val timerReward = taskSystem.claimReward(userId, timerTask.taskId)
        val stepsReward = taskSystem.claimReward(userId, stepsTask.taskId)
        val voiceReward = taskSystem.claimReward(userId, voiceTask.taskId)

        assertNotNull(timerReward)
        assertNotNull(stepsReward)
        assertNotNull(voiceReward)

        rewardDispatcher.dispatchRewards(userId, timerReward)
        rewardDispatcher.dispatchRewards(userId, stepsReward)
        rewardDispatcher.dispatchRewards(userId, voiceReward)

        assertEquals(37, currencyLedger.getBalance(userId, CurrencyType.STAR_DUST))

        val baseStats = mapOf(
            "focus" to 100,
            "vitality" to 100,
            "mood" to 100
        )
        attributeCalculator.setBaseStats(userId, baseStats)

        val finalStats = attributeCalculator.calculateCurrentStats(userId)
        assertEquals(105, finalStats["focus"])
        assertEquals(106, finalStats["vitality"])
        assertEquals(105, finalStats["mood"])
    }

    @Test
    fun testMultimodalTaskWithDiminishingReturns() {
        val userId = "user_diminishing_001"
        val taskSystem = TaskSystem()
        val currencyLedger = CurrencyLedger()
        val attributeCalculator = AttributeCalculator()
        val rewardDispatcher = RewardDispatcher(currencyLedger, attributeCalculator)

        val repeatableTask = TaskDef(
            taskId = "task_drink_water",
            taskName = "喝水",
            taskDescription = "保持水分充足",
            taskType = TaskType.A,
            taskCategory = TaskCategory.HEALTH,
            requirements = TaskRequirements(
                targetValue = 1,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 10),
                statDelta = mapOf("vitality" to 3),
                items = emptyList(),
                buffs = emptyList()
            ),
            maxCompletionsPerDay = 3,
            diminishingReturns = true,
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        taskSystem.registerTask(repeatableTask)

        var totalReward = 0

        for (i in 1..3) {
            taskSystem.updateProgress(userId, repeatableTask.taskId, 1)
            val reward = taskSystem.claimReward(userId, repeatableTask.taskId)
            assertNotNull(reward)
            rewardDispatcher.dispatchRewards(userId, reward)
            totalReward += reward.currencies[CurrencyType.STAR_DUST] ?: 0
        }

        assertTrue(totalReward < 30, "Diminishing returns should reduce total reward")
        assertTrue(totalReward > 0, "Should still receive some reward")
    }
}
