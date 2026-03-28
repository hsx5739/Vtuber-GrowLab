package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import com.maincharacter.shared.HostState
import com.maincharacter.shared.StatDelta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RewardDispatcherTest {
    
    private val economyManager = EconomyManager()
    private val attributeCalculator = AttributeCalculator()
    private val buffManager = BuffManager()
    private val currencyLedger = CurrencyLedger()
    private val attributeLedger = AttributeLedger()
    private val dispatcher = RewardDispatcher(
        economyManager,
        attributeCalculator,
        buffManager,
        currencyLedger,
        attributeLedger
    )
    
    @Test
    fun testDispatchSimpleCurrencyReward() {
        val wallet = Wallet(starDust = 100, moonGlow = 50)
        val hostState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        
        val bundle = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 50),
            statDelta = emptyMap(),
            items = emptyList(),
            buffs = emptyList()
        )
        
        val result = dispatcher.dispatch(
            bundle,
            wallet,
            hostState,
            ReasonCode.TASK_COMPLETE
        )
        
        assertTrue(result.success)
        assertEquals(150, result.wallet.starDust)
        assertEquals(50, result.wallet.moonGlow)
        assertNotNull(result.traceId)
    }
    
    @Test
    fun testDispatchStatReward() {
        val wallet = Wallet(starDust = 100)
        val hostState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        
        val bundle = RewardBundle(
            currencies = emptyMap(),
            statDelta = mapOf(StatKey.FORTUNE to 10, StatKey.VITALITY to -5),
            items = emptyList(),
            buffs = emptyList()
        )
        
        val result = dispatcher.dispatch(
            bundle,
            wallet,
            hostState,
            ReasonCode.TASK_COMPLETE
        )
        
        assertTrue(result.success)
        assertEquals(60, result.hostState.fortune)
        assertEquals(45, result.hostState.vitality)
        assertEquals(50, result.hostState.mood)
    }
    
    @Test
    fun testDispatchMixedReward() {
        val wallet = Wallet(starDust = 100, moonGlow = 50)
        val hostState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        
        val bundle = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 50, CurrencyId.MOON_GLOW to 10),
            statDelta = mapOf(StatKey.FORTUNE to 10),
            items = listOf(ItemGrant(itemId = "item_001", count = 1)),
            buffs = listOf(BuffGrant(buffId = "buff_001", durationSec = 3600))
        )
        
        val result = dispatcher.dispatch(
            bundle,
            wallet,
            hostState,
            ReasonCode.TASK_COMPLETE
        )
        
        assertTrue(result.success)
        assertEquals(150, result.wallet.starDust)
        assertEquals(60, result.wallet.moonGlow)
        assertEquals(60, result.hostState.fortune)
    }
    
    @Test
    fun testDispatchIdempotency() {
        val wallet = Wallet(starDust = 100)
        val hostState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        
        val bundle = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 50),
            statDelta = emptyMap(),
            items = emptyList(),
            buffs = emptyList()
        )
        
        val metadata = mapOf("suffix" to "test_idempotency")
        
        val result1 = dispatcher.dispatch(
            bundle,
            wallet,
            hostState,
            ReasonCode.TASK_COMPLETE,
            metadata
        )
        
        assertTrue(result1.success)
        assertEquals(150, result1.wallet.starDust)
        
        val result2 = dispatcher.dispatch(
            bundle,
            wallet,
            hostState,
            ReasonCode.TASK_COMPLETE,
            metadata
        )
        
        assertFalse(result2.success)
        assertEquals("Reward already dispatched with traceId:", result2.error?.substring(0, 40))
    }
    
    @Test
    fun testDispatchInvalidBundle() {
        val wallet = Wallet(starDust = 100)
        val hostState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        
        val bundle = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to -50),
            statDelta = emptyMap(),
            items = emptyList(),
            buffs = emptyList()
        )
        
        val result = dispatcher.dispatch(
            bundle,
            wallet,
            hostState,
            ReasonCode.TASK_COMPLETE
        )
        
        assertFalse(result.success)
        assertEquals("Invalid reward bundle:", result.error?.substring(0, 25))
    }
    
    @Test
    fun testDispatchBatch() {
        val wallet = Wallet(starDust = 100)
        val hostState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        
        val bundles = listOf(
            RewardBundle(currencies = mapOf(CurrencyId.STAR_DUST to 50)),
            RewardBundle(statDelta = mapOf(StatKey.FORTUNE to 10)),
            RewardBundle(currencies = mapOf(CurrencyId.MOON_GLOW to 10))
        )
        
        val result = dispatcher.dispatchBatch(
            bundles,
            wallet,
            hostState,
            ReasonCode.TASK_COMPLETE
        )
        
        assertTrue(result.success)
        assertEquals(150, result.wallet.starDust)
        assertEquals(10, result.wallet.moonGlow)
        assertEquals(60, result.hostState.fortune)
        assertEquals(3, result.results.size)
    }
    
    @Test
    fun testDispatchBatchPartialFailure() {
        val wallet = Wallet(starDust = 100)
        val hostState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        
        val bundles = listOf(
            RewardBundle(currencies = mapOf(CurrencyId.STAR_DUST to 50)),
            RewardBundle(currencies = mapOf(CurrencyId.STAR_DUST to -50)),
            RewardBundle(statDelta = mapOf(StatKey.FORTUNE to 10))
        )
        
        val result = dispatcher.dispatchBatch(
            bundles,
            wallet,
            hostState,
            ReasonCode.TASK_COMPLETE
        )
        
        assertFalse(result.success)
        assertEquals(100, result.wallet.starDust)
        assertEquals(50, result.hostState.fortune)
        assertTrue(result.results[0].success)
        assertFalse(result.results[1].success)
    }
    
    @Test
    fun testRollback() {
        val wallet = Wallet(starDust = 100)
        val hostState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        
        val bundle = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 50),
            statDelta = emptyMap(),
            items = emptyList(),
            buffs = emptyList()
        )
        
        val result = dispatcher.dispatch(
            bundle,
            wallet,
            hostState,
            ReasonCode.TASK_COMPLETE
        )
        
        assertTrue(result.success)
        assertTrue(dispatcher.hasDispatched(result.traceId))
        
        val rollbackResult = dispatcher.rollback(result.traceId)
        assertTrue(rollbackResult.success)
        assertFalse(dispatcher.hasDispatched(result.traceId))
    }
    
    @Test
    fun testRollbackNonExistent() {
        val rollbackResult = dispatcher.rollback("non_existent_trace_id")
        assertFalse(rollbackResult.success)
        assertEquals("No dispatched reward found with traceId:", rollbackResult.error?.substring(0, 40))
    }
    
    @Test
    fun testClearDispatchedRewards() {
        val wallet = Wallet(starDust = 100)
        val hostState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        
        val bundle = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 50),
            statDelta = emptyMap(),
            items = emptyList(),
            buffs = emptyList()
        )
        
        val result = dispatcher.dispatch(
            bundle,
            wallet,
            hostState,
            ReasonCode.TASK_COMPLETE
        )
        
        assertTrue(result.success)
        assertTrue(dispatcher.hasDispatched(result.traceId))
        
        dispatcher.clearDispatchedRewards()
        assertFalse(dispatcher.hasDispatched(result.traceId))
    }
}

class RewardBundleTest {
    
    @Test
    fun testIsEmpty() {
        val emptyBundle = RewardBundle()
        assertTrue(emptyBundle.isEmpty())
        
        val nonEmptyBundle = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 50)
        )
        assertFalse(nonEmptyBundle.isEmpty())
    }
    
    @Test
    fun testGetTotalValue() {
        val bundle = RewardBundle(
            currencies = mapOf(
                CurrencyId.STAR_DUST to 100,
                CurrencyId.MOON_GLOW to 10
            )
        )
        
        val totalValue = bundle.getTotalValue()
        assertEquals(200, totalValue)
    }
    
    @Test
    fun testGetCurrencies() {
        val bundle = RewardBundle(
            currencies = mapOf(
                CurrencyId.STAR_DUST to 100,
                CurrencyId.MOON_GLOW to 10
            )
        )
        
        val currencies = bundle.getCurrencies()
        assertEquals(2, currencies.size)
        
        val starDust = currencies.find { it.currencyId == CurrencyId.STAR_DUST }
        assertNotNull(starDust)
        assertEquals(100, starDust.amount)
        assertEquals("星尘", starDust.name)
        assertEquals("✨", starDust.icon)
    }
    
    @Test
    fun testGetStatDeltas() {
        val bundle = RewardBundle(
            statDelta = mapOf(
                StatKey.FORTUNE to 10,
                StatKey.VITALITY to -5
            )
        )
        
        val deltas = bundle.getStatDeltas()
        assertEquals(2, deltas.size)
        
        val fortuneDelta = deltas.find { it.key == StatKey.FORTUNE }
        assertNotNull(fortuneDelta)
        assertEquals(10, fortuneDelta.value)
    }
    
    @Test
    fun testHasCurrency() {
        val bundle = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 100)
        )
        
        assertTrue(bundle.hasCurrency(CurrencyId.STAR_DUST))
        assertFalse(bundle.hasCurrency(CurrencyId.MOON_GLOW))
    }
    
    @Test
    fun testHasStatDelta() {
        val bundle = RewardBundle(
            statDelta = mapOf(StatKey.FORTUNE to 10)
        )
        
        assertTrue(bundle.hasStatDelta(StatKey.FORTUNE))
        assertFalse(bundle.hasStatDelta(StatKey.VITALITY))
    }
    
    @Test
    fun testHasItem() {
        val bundle = RewardBundle(
            items = listOf(ItemGrant(itemId = "item_001", count = 1))
        )
        
        assertTrue(bundle.hasItem("item_001"))
        assertFalse(bundle.hasItem("item_002"))
    }
    
    @Test
    fun testHasBuff() {
        val bundle = RewardBundle(
            buffs = listOf(BuffGrant(buffId = "buff_001", durationSec = 3600))
        )
        
        assertTrue(bundle.hasBuff("buff_001"))
        assertFalse(bundle.hasBuff("buff_002"))
    }
    
    @Test
    fun testValidateValidBundle() {
        val bundle = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 100),
            statDelta = mapOf(StatKey.FORTUNE to 10),
            items = listOf(ItemGrant(itemId = "item_001", count = 1)),
            buffs = listOf(BuffGrant(buffId = "buff_001", durationSec = 3600))
        )
        
        val validation = bundle.validate()
        assertTrue(validation.isValid)
        assertTrue(validation.errors.isEmpty())
    }
    
    @Test
    fun testValidateInvalidBundle() {
        val bundle = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to -50),
            statDelta = mapOf(StatKey.FORTUNE to 0),
            items = listOf(ItemGrant(itemId = "", count = 0)),
            buffs = listOf(BuffGrant(buffId = "", durationSec = -1))
        )
        
        val validation = bundle.validate()
        assertFalse(validation.isValid)
        assertTrue(validation.errors.isNotEmpty())
    }
}

class ItemGrantTest {
    
    @Test
    fun testValidateValidItem() {
        val item = ItemGrant(itemId = "item_001", count = 1)
        val validation = item.validate()
        
        assertTrue(validation.isValid)
        assertTrue(validation.errors.isEmpty())
    }
    
    @Test
    fun testValidateInvalidItem() {
        val item = ItemGrant(itemId = "", count = 0)
        val validation = item.validate()
        
        assertFalse(validation.isValid)
        assertTrue(validation.errors.isNotEmpty())
    }
    
    @Test
    fun testValidateItemCountTooHigh() {
        val item = ItemGrant(itemId = "item_001", count = 1000)
        val validation = item.validate()
        
        assertFalse(validation.isValid)
        assertTrue(validation.errors.any { it.contains("exceed") })
    }
}

class BuffGrantTest {
    
    @Test
    fun testValidateValidBuff() {
        val buff = BuffGrant(buffId = "buff_001", durationSec = 3600)
        val validation = buff.validate()
        
        assertTrue(validation.isValid)
        assertTrue(validation.errors.isEmpty())
    }
    
    @Test
    fun testValidateInvalidBuff() {
        val buff = BuffGrant(buffId = "", durationSec = -1)
        val validation = buff.validate()
        
        assertFalse(validation.isValid)
        assertTrue(validation.errors.isNotEmpty())
    }
    
    @Test
    fun testValidateBuffDurationTooLong() {
        val buff = BuffGrant(buffId = "buff_001", durationSec = 86400 * 8)
        val validation = buff.validate()
        
        assertFalse(validation.isValid)
        assertTrue(validation.errors.any { it.contains("exceed") })
    }
}