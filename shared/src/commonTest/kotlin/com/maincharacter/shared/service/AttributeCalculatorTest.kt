package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import com.maincharacter.shared.StatDelta
import com.maincharacter.shared.HostState
import com.maincharacter.shared.Buff
import com.maincharacter.shared.StatKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class AttributeCalculatorTest {
    
    private val calculator = AttributeCalculator()
    
    @Test
    fun testApplyDelta_IncreaseValue() {
        val initialState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        val delta = StatDelta(StatKey.FORTUNE, 10)
        
        val newState = calculator.applyDelta(initialState, delta, "test")
        
        assertEquals(60, newState.fortune)
        assertEquals(50, newState.vitality)
        assertEquals(50, newState.mood)
        assertEquals(50, newState.bond)
        assertEquals(50, newState.focus)
        
        val changes = calculator.getChanges()
        assertEquals(1, changes.size)
        assertEquals(StatKey.FORTUNE, changes[0].key)
        assertEquals(50, changes[0].oldValue)
        assertEquals(60, changes[0].newValue)
        assertEquals(10, changes[0].delta)
        assertEquals("test", changes[0].reason)
    }
    
    @Test
    fun testApplyDelta_DecreaseValue() {
        val initialState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        val delta = StatDelta(StatKey.VITALITY, -20)
        
        val newState = calculator.applyDelta(initialState, delta, "test")
        
        assertEquals(50, newState.fortune)
        assertEquals(30, newState.vitality)
        assertEquals(50, newState.mood)
        assertEquals(50, newState.bond)
        assertEquals(50, newState.focus)
    }
    
    @Test
    fun testApplyDelta_ClampToMax() {
        val initialState = HostState(fortune = 90, vitality = 50, mood = 50, bond = 50, focus = 50)
        val delta = StatDelta(StatKey.FORTUNE, 20)
        
        val newState = calculator.applyDelta(initialState, delta, "test")
        
        assertEquals(100, newState.fortune)
    }
    
    @Test
    fun testApplyDelta_ClampToMin() {
        val initialState = HostState(fortune = 10, vitality = 50, mood = 50, bond = 50, focus = 50)
        val delta = StatDelta(StatKey.FORTUNE, -20)
        
        val newState = calculator.applyDelta(initialState, delta, "test")
        
        assertEquals(0, newState.fortune)
    }
    
    @Test
    fun testApplyDeltas_MultipleDeltas() {
        val initialState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        val deltas = listOf(
            StatDelta(StatKey.FORTUNE, 10),
            StatDelta(StatKey.VITALITY, -5),
            StatDelta(StatKey.MOOD, 15)
        )
        
        val newState = calculator.applyDeltas(initialState, deltas, "test")
        
        assertEquals(60, newState.fortune)
        assertEquals(45, newState.vitality)
        assertEquals(65, newState.mood)
        assertEquals(50, newState.bond)
        assertEquals(50, newState.focus)
        
        val changes = calculator.getChanges()
        assertEquals(3, changes.size)
    }
    
    @Test
    fun testApplyRewardBundle() {
        val initialState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        val rewardBundle = RewardBundle(
            traceId = "test-001",
            reasonCode = RewardReasonCode.TASK_COMPLETE,
            statDelta = mapOf(
                StatKey.FORTUNE to 10,
                StatKey.VITALITY to 5,
                StatKey.MOOD to -5
            ),
            currencyDelta = emptyMap(),
            items = emptyList(),
            buffs = emptyList()
        )
        
        val newState = calculator.applyRewardBundle(initialState, rewardBundle, "task")
        
        assertEquals(60, newState.fortune)
        assertEquals(55, newState.vitality)
        assertEquals(45, newState.mood)
        assertEquals(50, newState.bond)
        assertEquals(50, newState.focus)
    }
    
    @Test
    fun testCalculateWithBuffs_NoBuffs() {
        val initialState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        val buffDefs = emptyMap<String, BuffDef>()
        
        val result = calculator.calculateWithBuffs(initialState, buffDefs)
        
        assertEquals(50, result[StatKey.FORTUNE])
        assertEquals(50, result[StatKey.VITALITY])
        assertEquals(50, result[StatKey.MOOD])
        assertEquals(50, result[StatKey.BOND])
        assertEquals(50, result[StatKey.FOCUS])
    }
    
    @Test
    fun testCalculateWithBuffs_FlatBonus() {
        val initialState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        val buffDefs = mapOf(
            "buff1" to BuffDef(
                id = "buff1",
                name = "Test Buff",
                description = "Test",
                durationSec = 3600,
                statMultiplier = emptyMap(),
                statFlatBonus = mapOf(StatKey.FORTUNE to 10, StatKey.VITALITY to 5)
            )
        )
        
        val activeBuffs = listOf(
            Buff(buffId = "buff1", durationSec = 3600, startTime = System.currentTimeMillis() - 1000)
        )
        
        val stateWithBuffs = initialState.copy(activeBuffs = activeBuffs)
        val result = calculator.calculateWithBuffs(stateWithBuffs, buffDefs)
        
        assertEquals(60, result[StatKey.FORTUNE])
        assertEquals(55, result[StatKey.VITALITY])
        assertEquals(50, result[StatKey.MOOD])
        assertEquals(50, result[StatKey.BOND])
        assertEquals(50, result[StatKey.FOCUS])
    }
    
    @Test
    fun testCalculateWithBuffs_Multiplier() {
        val initialState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        val buffDefs = mapOf(
            "buff1" to BuffDef(
                id = "buff1",
                name = "Test Buff",
                description = "Test",
                durationSec = 3600,
                statMultiplier = mapOf(StatKey.FORTUNE to 1.2f, StatKey.VITALITY to 0.8f),
                statFlatBonus = emptyMap()
            )
        )
        
        val activeBuffs = listOf(
            Buff(buffId = "buff1", durationSec = 3600, startTime = System.currentTimeMillis() - 1000)
        )
        
        val stateWithBuffs = initialState.copy(activeBuffs = activeBuffs)
        val result = calculator.calculateWithBuffs(stateWithBuffs, buffDefs)
        
        assertEquals(60, result[StatKey.FORTUNE])
        assertEquals(40, result[StatKey.VITALITY])
        assertEquals(50, result[StatKey.MOOD])
        assertEquals(50, result[StatKey.BOND])
        assertEquals(50, result[StatKey.FOCUS])
    }
    
    @Test
    fun testCalculateWithBuffs_ExpiredBuff() {
        val initialState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        val buffDefs = mapOf(
            "buff1" to BuffDef(
                id = "buff1",
                name = "Test Buff",
                description = "Test",
                durationSec = 3600,
                statMultiplier = emptyMap(),
                statFlatBonus = mapOf(StatKey.FORTUNE to 10)
            )
        )
        
        val activeBuffs = listOf(
            Buff(buffId = "buff1", durationSec = 3600, startTime = System.currentTimeMillis() - 4000000)
        )
        
        val stateWithBuffs = initialState.copy(activeBuffs = activeBuffs)
        val result = calculator.calculateWithBuffs(stateWithBuffs, buffDefs)
        
        assertEquals(50, result[StatKey.FORTUNE])
    }
    
    @Test
    fun testCalculateWithBuffs_ClampToConstraints() {
        val initialState = HostState(fortune = 95, vitality = 50, mood = 50, bond = 50, focus = 50)
        val buffDefs = mapOf(
            "buff1" to BuffDef(
                id = "buff1",
                name = "Test Buff",
                description = "Test",
                durationSec = 3600,
                statMultiplier = emptyMap(),
                statFlatBonus = mapOf(StatKey.FORTUNE to 10)
            )
        )
        
        val activeBuffs = listOf(
            Buff(buffId = "buff1", durationSec = 3600, startTime = System.currentTimeMillis() - 1000)
        )
        
        val stateWithBuffs = initialState.copy(activeBuffs = activeBuffs)
        val result = calculator.calculateWithBuffs(stateWithBuffs, buffDefs)
        
        assertEquals(100, result[StatKey.FORTUNE])
    }
    
    @Test
    fun testGetAttributeValue() {
        val state = HostState(fortune = 10, vitality = 20, mood = 30, bond = 40, focus = 50)
        
        assertEquals(10, calculator.getAttributeValue(state, StatKey.FORTUNE))
        assertEquals(20, calculator.getAttributeValue(state, StatKey.VITALITY))
        assertEquals(30, calculator.getAttributeValue(state, StatKey.MOOD))
        assertEquals(40, calculator.getAttributeValue(state, StatKey.BOND))
        assertEquals(50, calculator.getAttributeValue(state, StatKey.FOCUS))
    }
    
    @Test
    fun testSetAttributeValue() {
        val state = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        
        val newState = calculator.setAttributeValue(state, StatKey.FORTUNE, 75)
        
        assertEquals(75, newState.fortune)
        assertEquals(50, newState.vitality)
        assertEquals(50, newState.mood)
        assertEquals(50, newState.bond)
        assertEquals(50, newState.focus)
    }
    
    @Test
    fun testClampValue() {
        assertEquals(0, calculator.clampValue(StatKey.FORTUNE, -10))
        assertEquals(50, calculator.clampValue(StatKey.FORTUNE, 50))
        assertEquals(100, calculator.clampValue(StatKey.FORTUNE, 150))
    }
    
    @Test
    fun testIsValueInRange() {
        assertTrue(calculator.isValueInRange(StatKey.FORTUNE, 50))
        assertTrue(calculator.isValueInRange(StatKey.FORTUNE, 0))
        assertTrue(calculator.isValueInRange(StatKey.FORTUNE, 100))
        assertFalse(calculator.isValueInRange(StatKey.FORTUNE, -1))
        assertFalse(calculator.isValueInRange(StatKey.FORTUNE, 101))
    }
    
    @Test
    fun testGetAttributePercentage() {
        val state = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        
        assertEquals(0.5f, calculator.getAttributePercentage(state, StatKey.FORTUNE))
        assertEquals(0.5f, calculator.getAttributePercentage(state, StatKey.VITALITY))
    }
    
    @Test
    fun testGetSnapshot() {
        val state = HostState(fortune = 10, vitality = 20, mood = 30, bond = 40, focus = 50)
        
        val snapshot = calculator.getSnapshot(state)
        
        assertEquals(10, snapshot.fortune)
        assertEquals(20, snapshot.vitality)
        assertEquals(30, snapshot.mood)
        assertEquals(40, snapshot.bond)
        assertEquals(50, snapshot.focus)
    }
    
    @Test
    fun testClearChanges() {
        val initialState = HostState(fortune = 50, vitality = 50, mood = 50, bond = 50, focus = 50)
        val delta = StatDelta(StatKey.FORTUNE, 10)
        
        calculator.applyDelta(initialState, delta, "test")
        assertEquals(1, calculator.getChanges().size)
        
        calculator.clearChanges()
        assertEquals(0, calculator.getChanges().size)
    }
    
    @Test
    fun testCustomConstraints() {
        val customConstraints = AttributeConstraints(
            fortune = 0..200,
            vitality = 0..150,
            mood = 0..100,
            bond = 0..100,
            focus = 0..100
        )
        val customCalculator = AttributeCalculator(customConstraints)
        
        val initialState = HostState(fortune = 150, vitality = 50, mood = 50, bond = 50, focus = 50)
        val delta = StatDelta(StatKey.FORTUNE, 100)
        
        val newState = customCalculator.applyDelta(initialState, delta, "test")
        
        assertEquals(200, newState.fortune)
    }
}