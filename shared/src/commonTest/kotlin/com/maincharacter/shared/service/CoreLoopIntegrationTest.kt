package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CoreLoopIntegrationTest {

    @Test
    fun testCompleteCoreLoop() {
        val userId = "user_001"
        val now = System.currentTimeMillis()

        val signInSystem = SignInSystem()
        val taskSystem = TaskSystem()
        val currencyLedger = CurrencyLedger()
        val attributeCalculator = AttributeCalculator()
        val rewardDispatcher = RewardDispatcher(currencyLedger, attributeCalculator)
        val gachaSystem = GachaSystem()

        val signInConfig = SignInConfig(
            dailyRewards = mapOf(
                1 to RewardBundle(
                    currencies = mapOf(CurrencyType.STAR_DUST to 100),
                    statDelta = emptyMap(),
                    items = emptyList(),
                    buffs = emptyList()
                ),
                2 to RewardBundle(
                    currencies = mapOf(CurrencyType.STAR_DUST to 150),
                    statDelta = emptyMap(),
                    items = emptyList(),
                    buffs = emptyList()
                )
            ),
            weeklyRewards = mapOf(
                7 to RewardBundle(
                    currencies = mapOf(CurrencyType.STAR_DUST to 500),
                    statDelta = emptyMap(),
                    items = listOf(
                        RewardItem(itemId = "item_001", count = 1)
                    ),
                    buffs = emptyList()
                )
            )
        )

        val dailyTask = TaskDef(
            taskId = "task_daily_001",
            taskName = "Daily Login",
            taskDescription = "Login daily",
            taskType = TaskType.DAILY,
            taskCategory = TaskCategory.SOCIAL,
            requirements = TaskRequirements(
                targetValue = 1,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 50),
                statDelta = mapOf("attack" to 5),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = now - 10000,
            endTime = now + 10000
        )

        val gachaPool = GachaPoolDef(
            poolId = "pool_001",
            poolName = "Standard Pool",
            poolDescription = "Standard Gacha Pool",
            poolType = GachaPoolType.STANDARD,
            cost = GachaCost(
                currencyType = CurrencyType.STAR_DUST,
                singlePullCost = 100,
                tenPullCost = 900
            ),
            rows = listOf(
                GachaRowDef(
                    rowId = "row_001",
                    rowName = "R Row",
                    rowDescription = "R Items",
                    rarity = SkinRarity.R,
                    items = listOf(
                        GachaItemDef(
                            itemId = "skin_001",
                            itemName = "Skin 001",
                            itemDescription = "Description",
                            probability = 1.0f,
                            isLimited = false
                        )
                    )
                )
            ),
            pityRules = PityRules(
                rarePullsMax = 10,
                rareRarity = SkinRarity.R,
                superRarePullsMax = 90,
                superRareRarity = SkinRarity.SR,
                ultraRarePullsMax = 200,
                ultraRareRarity = SkinRarity.SSR
            ),
            duplicateRules = DuplicateRules(),
            probabilityDisplay = ProbabilityDisplay(
                displayProbabilities = mapOf(
                    SkinRarity.N to 0.6f,
                    SkinRarity.R to 0.3f,
                    SkinRarity.SR to 0.09f,
                    SkinRarity.SSR to 0.01f
                )
            ),
            startTime = now - 10000,
            endTime = now + 10000,
            isActive = true
        )

        taskSystem.registerTask(dailyTask)

        val signInResult = signInSystem.signIn(userId, now, signInConfig)
        assertTrue(signInResult.success)
        assertEquals(1, signInResult.consecutiveDays)
        assertEquals(100, signInResult.rewards.currencies[CurrencyType.STAR_DUST])

        rewardDispatcher.dispatchRewards(userId, signInResult.rewards)

        assertEquals(100, currencyLedger.getBalance(userId, CurrencyType.STAR_DUST))

        taskSystem.updateProgress(userId, dailyTask.taskId, 1)
        val taskProgress = taskSystem.getProgress(userId, dailyTask.taskId)
        assertNotNull(taskProgress)
        assertEquals(1, taskProgress.currentProgress)
        assertTrue(taskProgress.isCompleted)

        val taskReward = taskSystem.claimReward(userId, dailyTask.taskId)
        assertNotNull(taskReward)
        assertEquals(50, taskReward.currencies[CurrencyType.STAR_DUST])
        assertEquals(5, taskReward.statDelta["attack"])

        rewardDispatcher.dispatchRewards(userId, taskReward)

        assertEquals(150, currencyLedger.getBalance(userId, CurrencyType.STAR_DUST))

        val baseStats = mapOf(
            "attack" to 100,
            "defense" to 80,
            "health" to 1000
        )
        attributeCalculator.setBaseStats(userId, baseStats)

        val currentStats = attributeCalculator.calculateCurrentStats(userId)
        assertEquals(105, currentStats["attack"])
        assertEquals(80, currentStats["defense"])
        assertEquals(1000, currentStats["health"])

        val gachaCost = gachaPool.cost
        val balanceBeforeGacha = currencyLedger.getBalance(userId, gachaCost.currencyType)
        assertTrue(balanceBeforeGacha >= gachaCost.singlePullCost)

        val gachaResult = gachaSystem.pull(userId, gachaPool, 1)
        assertEquals(1, gachaResult.results.size)
        assertTrue(gachaResult.results[0].itemId.startsWith("skin_"))

        val balanceAfterGacha = currencyLedger.getBalance(userId, gachaCost.currencyType)
        assertEquals(balanceBeforeGacha - gachaCost.singlePullCost, balanceAfterGacha)
    }

    @Test
    fun testCoreLoopWithMultipleTasks() {
        val userId = "user_002"
        val now = System.currentTimeMillis()

        val taskSystem = TaskSystem()
        val currencyLedger = CurrencyLedger()
        val attributeCalculator = AttributeCalculator()
        val rewardDispatcher = RewardDispatcher(currencyLedger, attributeCalculator)

        val task1 = TaskDef(
            taskId = "task_001",
            taskName = "Complete Daily Quest",
            taskDescription = "Complete 1 daily quest",
            taskType = TaskType.DAILY,
            taskCategory = TaskCategory.GAMEPLAY,
            requirements = TaskRequirements(
                targetValue = 1,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 100),
                statDelta = mapOf("attack" to 10),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = now - 10000,
            endTime = now + 10000
        )

        val task2 = TaskDef(
            taskId = "task_002",
            taskName = "Use Skill Card",
            taskDescription = "Use 3 skill cards",
            taskType = TaskType.DAILY,
            taskCategory = TaskCategory.GAMEPLAY,
            requirements = TaskRequirements(
                targetValue = 3,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.COIN to 200),
                statDelta = mapOf("defense" to 8),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = now - 10000,
            endTime = now + 10000
        )

        val task3 = TaskDef(
            taskId = "task_003",
            taskName = "Gacha Pull",
            taskDescription = "Perform 1 gacha pull",
            taskType = TaskType.DAILY,
            taskCategory = TaskCategory.GACHA,
            requirements = TaskRequirements(
                targetValue = 1,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 50),
                statDelta = mapOf("health" to 50),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = now - 10000,
            endTime = now + 10000
        )

        taskSystem.registerTask(task1)
        taskSystem.registerTask(task2)
        taskSystem.registerTask(task3)

        taskSystem.updateProgress(userId, task1.taskId, 1)
        taskSystem.updateProgress(userId, task2.taskId, 3)
        taskSystem.updateProgress(userId, task3.taskId, 1)

        val reward1 = taskSystem.claimReward(userId, task1.taskId)
        val reward2 = taskSystem.claimReward(userId, task2.taskId)
        val reward3 = taskSystem.claimReward(userId, task3.taskId)

        assertNotNull(reward1)
        assertNotNull(reward2)
        assertNotNull(reward3)

        rewardDispatcher.dispatchRewards(userId, reward1)
        rewardDispatcher.dispatchRewards(userId, reward2)
        rewardDispatcher.dispatchRewards(userId, reward3)

        assertEquals(150, currencyLedger.getBalance(userId, CurrencyType.STAR_DUST))
        assertEquals(200, currencyLedger.getBalance(userId, CurrencyType.COIN))

        val baseStats = mapOf(
            "attack" to 100,
            "defense" to 80,
            "health" to 1000
        )
        attributeCalculator.setBaseStats(userId, baseStats)

        val currentStats = attributeCalculator.calculateCurrentStats(userId)
        assertEquals(110, currentStats["attack"])
        assertEquals(88, currentStats["defense"])
        assertEquals(1050, currentStats["health"])
    }

    @Test
    fun testCoreLoopWithWeeklySignInBonus() {
        val userId = "user_003"
        val now = System.currentTimeMillis()

        val signInSystem = SignInSystem()
        val currencyLedger = CurrencyLedger()
        val rewardDispatcher = RewardDispatcher(currencyLedger, AttributeCalculator())

        val signInConfig = SignInConfig(
            dailyRewards = mapOf(
                1 to RewardBundle(
                    currencies = mapOf(CurrencyType.STAR_DUST to 100),
                    statDelta = emptyMap(),
                    items = emptyList(),
                    buffs = emptyList()
                ),
                2 to RewardBundle(
                    currencies = mapOf(CurrencyType.STAR_DUST to 150),
                    statDelta = emptyMap(),
                    items = emptyList(),
                    buffs = emptyList()
                ),
                3 to RewardBundle(
                    currencies = mapOf(CurrencyType.STAR_DUST to 200),
                    statDelta = emptyMap(),
                    items = emptyList(),
                    buffs = emptyList()
                ),
                4 to RewardBundle(
                    currencies = mapOf(CurrencyType.STAR_DUST to 250),
                    statDelta = emptyMap(),
                    items = emptyList(),
                    buffs = emptyList()
                ),
                5 to RewardBundle(
                    currencies = mapOf(CurrencyType.STAR_DUST to 300),
                    statDelta = emptyMap(),
                    items = emptyList(),
                    buffs = emptyList()
                ),
                6 to RewardBundle(
                    currencies = mapOf(CurrencyType.STAR_DUST to 350),
                    statDelta = emptyMap(),
                    items = emptyList(),
                    buffs = emptyList()
                ),
                7 to RewardBundle(
                    currencies = mapOf(CurrencyType.STAR_DUST to 400),
                    statDelta = emptyMap(),
                    items = emptyList(),
                    buffs = emptyList()
                )
            ),
            weeklyRewards = mapOf(
                7 to RewardBundle(
                    currencies = mapOf(CurrencyType.STAR_DUST to 1000),
                    statDelta = emptyMap(),
                    items = listOf(
                        RewardItem(itemId = "item_weekly_001", count = 1)
                    ),
                    buffs = emptyList()
                )
            )
        )

        var totalStarDust = 0

        for (day in 1..7) {
            val signInTime = now + (day - 1) * 24 * 60 * 60 * 1000L
            val result = signInSystem.signIn(userId, signInTime, signInConfig)
            assertTrue(result.success)
            assertEquals(day, result.consecutiveDays)

            rewardDispatcher.dispatchRewards(userId, result.rewards)
            totalStarDust += result.rewards.currencies[CurrencyType.STAR_DUST] ?: 0

            if (day == 7) {
                assertTrue(result.rewards.items.isNotEmpty())
                assertEquals("item_weekly_001", result.rewards.items[0].itemId)
            }
        }

        assertEquals(2750, totalStarDust)
        assertEquals(2750, currencyLedger.getBalance(userId, CurrencyType.STAR_DUST))
    }

    @Test
    fun testCoreLoopWithGachaPitySystem() {
        val userId = "user_004"
        val now = System.currentTimeMillis()

        val currencyLedger = CurrencyLedger()
        val gachaSystem = GachaSystem()

        val gachaPool = GachaPoolDef(
            poolId = "pool_pity",
            poolName = "Pity Test Pool",
            poolDescription = "Pool for testing pity system",
            poolType = GachaPoolType.STANDARD,
            cost = GachaCost(
                currencyType = CurrencyType.STAR_DUST,
                singlePullCost = 10,
                tenPullCost = 90
            ),
            rows = listOf(
                GachaRowDef(
                    rowId = "row_n",
                    rowName = "N Row",
                    rowDescription = "N Items",
                    rarity = SkinRarity.N,
                    items = listOf(
                        GachaItemDef(
                            itemId = "skin_n_001",
                            itemName = "N Skin 001",
                            itemDescription = "Description",
                            probability = 1.0f,
                            isLimited = false
                        )
                    )
                ),
                GachaRowDef(
                    rowId = "row_r",
                    rowName = "R Row",
                    rowDescription = "R Items",
                    rarity = SkinRarity.R,
                    items = listOf(
                        GachaItemDef(
                            itemId = "skin_r_001",
                            itemName = "R Skin 001",
                            itemDescription = "Description",
                            probability = 1.0f,
                            isLimited = false
                        )
                    )
                )
            ),
            pityRules = PityRules(
                rarePullsMax = 5,
                rareRarity = SkinRarity.R,
                superRarePullsMax = 90,
                superRareRarity = SkinRarity.SR,
                ultraRarePullsMax = 200,
                ultraRareRarity = SkinRarity.SSR
            ),
            duplicateRules = DuplicateRules(),
            probabilityDisplay = ProbabilityDisplay(
                displayProbabilities = mapOf(
                    SkinRarity.N to 0.9f,
                    SkinRarity.R to 0.1f,
                    SkinRarity.SR to 0.0f,
                    SkinRarity.SSR to 0.0f
                )
            ),
            startTime = now - 10000,
            endTime = now + 10000,
            isActive = true
        )

        currencyLedger.add(userId, CurrencyType.STAR_DUST, 1000)

        var pullCount = 0
        var foundRare = false

        while (!foundRare && pullCount < 10) {
            val result = gachaSystem.pull(userId, gachaPool, 1)
            pullCount++
            
            val item = result.results[0]
            if (item.rarity == SkinRarity.R) {
                foundRare = true
                assertTrue(pullCount <= 5, "Pity should trigger at or before pull 5")
            }
        }

        assertTrue(foundRare, "Should have found R item due to pity")
    }

    @Test
    fun testCoreLoopWithAttributeBonuses() {
        val userId = "user_005"
        val now = System.currentTimeMillis()

        val attributeCalculator = AttributeCalculator()
        val currencyLedger = CurrencyLedger()
        val rewardDispatcher = RewardDispatcher(currencyLedger, attributeCalculator)

        val baseStats = mapOf(
            "attack" to 100,
            "defense" to 80,
            "health" to 1000,
            "speed" to 50
        )
        attributeCalculator.setBaseStats(userId, baseStats)

        val reward1 = RewardBundle(
            currencies = emptyMap(),
            statDelta = mapOf(
                "attack" to 20,
                "defense" to 10,
                "health" to 100
            ),
            items = emptyList(),
            buffs = emptyList()
        )

        val reward2 = RewardBundle(
            currencies = emptyMap(),
            statDelta = mapOf(
                "attack" to 30,
                "speed" to 10
            ),
            items = emptyList(),
            buffs = emptyList()
        )

        rewardDispatcher.dispatchRewards(userId, reward1)
        rewardDispatcher.dispatchRewards(userId, reward2)

        val finalStats = attributeCalculator.calculateCurrentStats(userId)
        assertEquals(150, finalStats["attack"])
        assertEquals(90, finalStats["defense"])
        assertEquals(1100, finalStats["health"])
        assertEquals(60, finalStats["speed"])
    }
}
