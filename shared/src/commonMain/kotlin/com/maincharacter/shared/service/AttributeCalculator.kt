package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AttributeCalculator(
    private val constraints: AttributeConstraints = AttributeConstraints()
) {
    
    private val _attributeChanges = MutableStateFlow<List<AttributeChange>>(emptyList())
    val attributeChanges: StateFlow<List<AttributeChange>> = _attributeChanges
    
    fun applyDelta(
        currentState: HostState,
        delta: StatDelta,
        reason: String = "unknown"
    ): HostState {
        val oldValue = getAttributeValue(currentState, delta.key)
        val newValue = constraints.clamp(delta.key, oldValue + delta.value)
        val actualDelta = newValue - oldValue
        
        val updatedState = setAttributeValue(currentState, delta.key, newValue)
        
        val change = AttributeChange(
            key = delta.key,
            oldValue = oldValue,
            newValue = newValue,
            delta = actualDelta,
            reason = reason
        )
        
        _attributeChanges.value = _attributeChanges.value + change
        
        return updatedState
    }

    fun applyDelta(
        currentState: HostState,
        key: StatKey,
        value: Int,
        reason: String = "unknown"
    ): HostState = applyDelta(currentState, StatDelta(key, value), reason)
    
    fun applyDeltas(
        currentState: HostState,
        deltas: List<StatDelta>,
        reason: String = "unknown"
    ): HostState {
        var state = currentState
        deltas.forEach { delta ->
            state = applyDelta(state, delta, reason)
        }
        return state
    }
    
    fun applyRewardBundle(
        currentState: HostState,
        rewardBundle: RewardBundle,
        reason: String = "reward"
    ): HostState {
        val deltas = rewardBundle.statDelta.map { (key, value) ->
            StatDelta(key, value)
        }
        return applyDeltas(currentState, deltas, reason)
    }
    
    fun calculateWithBuffs(
        currentState: HostState,
        buffDefs: Map<String, BuffDef>
    ): Map<StatKey, Int> {
        val baseValues = mapOf(
            StatKey.FORTUNE to currentState.fortune,
            StatKey.VITALITY to currentState.vitality,
            StatKey.MOOD to currentState.mood,
            StatKey.BOND to currentState.bond,
            StatKey.FOCUS to currentState.focus
        )
        
        val activeBuffs = currentState.activeBuffs.filter { buff ->
            val elapsedSec = (System.currentTimeMillis() - buff.startTime) / 1000
            elapsedSec < buff.durationSec
        }
        
        val finalValues = mutableMapOf<StatKey, Int>()
        
        StatKey.values().forEach { key ->
            var value = baseValues[key] ?: 0
            
            activeBuffs.forEach { buff ->
                val buffDef = buffDefs[buff.buffId] ?: return@forEach
                
                buffDef.statMultiplier[key]?.let { multiplier ->
                    value = (value * multiplier).toInt()
                }
                
                buffDef.statFlatBonus[key]?.let { bonus ->
                    value += bonus
                }
            }
            
            finalValues[key] = constraints.clamp(key, value)
        }
        
        return finalValues
    }
    
    fun getAttributeValue(state: HostState, key: StatKey): Int {
        return when (key) {
            StatKey.FORTUNE -> state.fortune
            StatKey.VITALITY -> state.vitality
            StatKey.MOOD -> state.mood
            StatKey.BOND -> state.bond
            StatKey.FOCUS -> state.focus
        }
    }
    
    fun setAttributeValue(state: HostState, key: StatKey, value: Int): HostState {
        return when (key) {
            StatKey.FORTUNE -> state.copy(fortune = value)
            StatKey.VITALITY -> state.copy(vitality = value)
            StatKey.MOOD -> state.copy(mood = value)
            StatKey.BOND -> state.copy(bond = value)
            StatKey.FOCUS -> state.copy(focus = value)
        }
    }
    
    fun clampValue(key: StatKey, value: Int): Int {
        return constraints.clamp(key, value)
    }
    
    fun isValueInRange(key: StatKey, value: Int): Boolean {
        return value in constraints.getRange(key)
    }
    
    fun getAttributePercentage(state: HostState, key: StatKey): Float {
        val value = getAttributeValue(state, key)
        val max = constraints.getRange(key).last
        return value.toFloat() / max.toFloat()
    }
    
    fun getSnapshot(state: HostState): AttributeSnapshot {
        return AttributeSnapshot(
            fortune = state.fortune,
            vitality = state.vitality,
            mood = state.mood,
            bond = state.bond,
            focus = state.focus
        )
    }
    
    fun clearChanges() {
        _attributeChanges.value = emptyList()
    }
    
    fun getChanges(): List<AttributeChange> {
        return _attributeChanges.value
    }
}
