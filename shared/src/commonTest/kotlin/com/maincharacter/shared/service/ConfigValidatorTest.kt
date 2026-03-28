package com.maincharacter.shared.service

import com.maincharacter.shared.config.ConfigValidator
import com.maincharacter.shared.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfigValidatorTest {

    private val validator = ConfigValidator()

    @Test
    fun testValidateRewardBundle_ValidBundle() {
        val bundle = RewardBundle(
            currencies = mapOf(
                CurrencyType.STAR_DUST to 100,
                CurrencyType.COIN to 50
            ),
            statDelta = mapOf(
                "attack" to 10,
                "defense" to 5
            ),
            items = listOf(
                RewardItem(itemId = "item_001", count = 1),
                RewardItem(itemId = "item_002", count = 2)
            ),
            buffs = listOf(
                RewardBuff(buffId = "buff_001", durationSec = 3600),
                RewardBuff(buffId = "buff_002", durationSec = 7200)
            )
        )

        val result = validator.validateRewardBundle(bundle)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun testValidateRewardBundle_NegativeCurrencyAmount() {
        val bundle = RewardBundle(
            currencies = mapOf(
                CurrencyType.STAR_DUST to -100,
                CurrencyType.COIN to 50
            ),
            statDelta = emptyMap(),
            items = emptyList(),
            buffs = emptyList()
        )

        val result = validator.validateRewardBundle(bundle)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Currency amount must be >= 0") })
    }

    @Test
    fun testValidateRewardBundle_InvalidStatKey() {
        val bundle = RewardBundle(
            currencies = emptyMap(),
            statDelta = mapOf(
                "invalid_stat" to 10,
                "defense" to 5
            ),
            items = emptyList(),
            buffs = emptyList()
        )

        val result = validator.validateRewardBundle(bundle)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Invalid stat key") })
    }

    @Test
    fun testValidateRewardBundle_InvalidItemCount() {
        val bundle = RewardBundle(
            currencies = emptyMap(),
            statDelta = emptyMap(),
            items = listOf(
                RewardItem(itemId = "item_001", count = 0),
                RewardItem(itemId = "item_002", count = 2)
            ),
            buffs = emptyList()
        )

        val result = validator.validateRewardBundle(bundle)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Item count must be >= 1") })
    }

    @Test
    fun testValidateRewardBundle_NegativeBuffDuration() {
        val bundle = RewardBundle(
            currencies = emptyMap(),
            statDelta = emptyMap(),
            items = emptyList(),
            buffs = listOf(
                RewardBuff(buffId = "buff_001", durationSec = -100),
                RewardBuff(buffId = "buff_002", durationSec = 3600)
            )
        )

        val result = validator.validateRewardBundle(bundle)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Buff duration must be >= 0") })
    }

    @Test
    fun testValidateTaskDef_ValidTask() {
        val task = TaskDef(
            taskId = "task_001",
            taskName = "Daily Login",
            taskDescription = "Login daily",
            taskType = TaskType.DAILY,
            taskCategory = TaskCategory.SOCIAL,
            requirements = TaskRequirements(
                targetValue = 1,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 100),
                statDelta = emptyMap(),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        val result = validator.validateTaskDef(task)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun testValidateTaskDef_InvalidTargetValue() {
        val task = TaskDef(
            taskId = "task_001",
            taskName = "Daily Login",
            taskDescription = "Login daily",
            taskType = TaskType.DAILY,
            taskCategory = TaskCategory.SOCIAL,
            requirements = TaskRequirements(
                targetValue = 0,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 100),
                statDelta = emptyMap(),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000
        )

        val result = validator.validateTaskDef(task)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Target value must be > 0") })
    }

    @Test
    fun testValidateTaskDef_InvalidTimeRange() {
        val now = System.currentTimeMillis()
        val task = TaskDef(
            taskId = "task_001",
            taskName = "Daily Login",
            taskDescription = "Login daily",
            taskType = TaskType.DAILY,
            taskCategory = TaskCategory.SOCIAL,
            requirements = TaskRequirements(
                targetValue = 1,
                currentProgress = 0
            ),
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 100),
                statDelta = emptyMap(),
                items = emptyList(),
                buffs = emptyList()
            ),
            isActive = true,
            startTime = now + 10000,
            endTime = now - 10000
        )

        val result = validator.validateTaskDef(task)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("End time must be after start time") })
    }

    @Test
    fun testValidateEventDef_ValidEvent() {
        val event = EventDef(
            eventId = "event_001",
            eventName = "Spring Festival",
            eventDescription = "Spring Festival Event",
            eventType = EventType.SPECIAL,
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000,
            isActive = true,
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 500),
                statDelta = emptyMap(),
                items = emptyList(),
                buffs = emptyList()
            )
        )

        val result = validator.validateEventDef(event)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun testValidateEventDef_InvalidTimeRange() {
        val now = System.currentTimeMillis()
        val event = EventDef(
            eventId = "event_001",
            eventName = "Spring Festival",
            eventDescription = "Spring Festival Event",
            eventType = EventType.SPECIAL,
            startTime = now + 10000,
            endTime = now - 10000,
            isActive = true,
            rewards = RewardBundle(
                currencies = mapOf(CurrencyType.STAR_DUST to 500),
                statDelta = emptyMap(),
                items = emptyList(),
                buffs = emptyList()
            )
        )

        val result = validator.validateEventDef(event)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("End time must be after start time") })
    }

    @Test
    fun testValidateGachaPool_ValidPool() {
        val now = System.currentTimeMillis()
        val pool = GachaPoolDef(
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
                            probability = 0.3f,
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
            duplicateRules = DuplicateRules(
                duplicateCurrency = CurrencyType.STAR_DUST,
                duplicateRatio = mapOf(
                    SkinRarity.N to 0.1f,
                    SkinRarity.R to 0.2f,
                    SkinRarity.SR to 0.5f,
                    SkinRarity.SSR to 1.0f
                )
            ),
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

        val result = validator.validateGachaPool(pool)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun testValidateGachaPool_InvalidCost() {
        val now = System.currentTimeMillis()
        val pool = GachaPoolDef(
            poolId = "pool_001",
            poolName = "Standard Pool",
            poolDescription = "Standard Gacha Pool",
            poolType = GachaPoolType.STANDARD,
            cost = GachaCost(
                currencyType = CurrencyType.STAR_DUST,
                singlePullCost = 100,
                tenPullCost = 1000
            ),
            rows = emptyList(),
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

        val result = validator.validateGachaPool(pool)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Ten pull cost must be less than 10 * single pull cost") })
    }

    @Test
    fun testValidateGachaPool_InvalidPityRules() {
        val now = System.currentTimeMillis()
        val pool = GachaPoolDef(
            poolId = "pool_001",
            poolName = "Standard Pool",
            poolDescription = "Standard Gacha Pool",
            poolType = GachaPoolType.STANDARD,
            cost = GachaCost(
                currencyType = CurrencyType.STAR_DUST,
                singlePullCost = 100,
                tenPullCost = 900
            ),
            rows = emptyList(),
            pityRules = PityRules(
                rarePullsMax = 10,
                rareRarity = SkinRarity.R,
                superRarePullsMax = 5,
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

        val result = validator.validateGachaPool(pool)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Super rare pulls max must be greater than rare pulls max") })
    }

    @Test
    fun testValidateGachaPool_InvalidProbabilitySum() {
        val now = System.currentTimeMillis()
        val pool = GachaPoolDef(
            poolId = "pool_001",
            poolName = "Standard Pool",
            poolDescription = "Standard Gacha Pool",
            poolType = GachaPoolType.STANDARD,
            cost = GachaCost(
                currencyType = CurrencyType.STAR_DUST,
                singlePullCost = 100,
                tenPullCost = 900
            ),
            rows = emptyList(),
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
                    SkinRarity.SSR to 0.02f
                )
            ),
            startTime = now - 10000,
            endTime = now + 10000,
            isActive = true
        )

        val result = validator.validateGachaPool(pool)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Display probabilities must sum to 1.0") })
    }

    @Test
    fun testValidateGachaPool_InvalidTimeRange() {
        val now = System.currentTimeMillis()
        val pool = GachaPoolDef(
            poolId = "pool_001",
            poolName = "Standard Pool",
            poolDescription = "Standard Gacha Pool",
            poolType = GachaPoolType.STANDARD,
            cost = GachaCost(
                currencyType = CurrencyType.STAR_DUST,
                singlePullCost = 100,
                tenPullCost = 900
            ),
            rows = emptyList(),
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
            startTime = now + 10000,
            endTime = now - 10000,
            isActive = true
        )

        val result = validator.validateGachaPool(pool)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("End time must be after start time") })
    }

    @Test
    fun testValidateGachaPool_RowProbabilitySum() {
        val now = System.currentTimeMillis()
        val pool = GachaPoolDef(
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
                            probability = 0.6f,
                            isLimited = false
                        ),
                        GachaItemDef(
                            itemId = "skin_002",
                            itemName = "Skin 002",
                            itemDescription = "Description",
                            probability = 0.6f,
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

        val result = validator.validateGachaPool(pool)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Row probabilities must sum to 1.0") })
    }
}
