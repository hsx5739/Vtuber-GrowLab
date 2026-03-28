package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AttributeLedger {
    
    private val _entries = MutableStateFlow<List<LedgerEntry>>(emptyList())
    val entries: StateFlow<List<LedgerEntry>> = _entries
    
    fun recordChange(
        key: StatKey,
        oldValue: Int,
        newValue: Int,
        reason: String,
        source: ChangeSource = ChangeSource.UNKNOWN
    ) {
        val entry = LedgerEntry(
            id = generateEntryId(),
            key = key,
            oldValue = oldValue,
            newValue = newValue,
            delta = newValue - oldValue,
            reason = reason,
            source = source,
            timestamp = System.currentTimeMillis()
        )
        
        _entries.value = _entries.value + entry
    }
    
    fun recordDelta(
        key: StatKey,
        delta: Int,
        currentValue: Int,
        reason: String,
        source: ChangeSource = ChangeSource.UNKNOWN
    ) {
        val newValue = currentValue + delta
        recordChange(key, currentValue, newValue, reason, source)
    }
    
    fun recordBundle(
        bundle: RewardBundle,
        currentState: HostState,
        source: ChangeSource = ChangeSource.REWARD
    ) {
        bundle.statDelta.forEach { (key, delta) ->
            val currentValue = getAttributeValue(currentState, key)
            recordDelta(key, delta, currentValue, "reward_bundle", source)
        }
    }
    
    fun recordBuffEffect(
        buffId: String,
        key: StatKey,
        multiplier: Float,
        flatBonus: Int,
        currentValue: Int,
        newValue: Int
    ) {
        val entry = LedgerEntry(
            id = generateEntryId(),
            key = key,
            oldValue = currentValue,
            newValue = newValue,
            delta = newValue - currentValue,
            reason = "buff_effect",
            source = ChangeSource.BUFF,
            metadata = mapOf(
                "buffId" to buffId,
                "multiplier" to multiplier.toString(),
                "flatBonus" to flatBonus.toString()
            ),
            timestamp = System.currentTimeMillis()
        )
        
        _entries.value = _entries.value + entry
    }
    
    fun recordTransaction(
        currencyId: CurrencyId,
        oldBalance: Int,
        newBalance: Int,
        reason: String,
        source: ChangeSource = ChangeSource.TRANSACTION
    ) {
        val entry = LedgerEntry(
            id = generateEntryId(),
            key = when (currencyId) {
                CurrencyId.STAR_DUST -> StatKey.FORTUNE
                CurrencyId.MOON_GLOW -> StatKey.FORTUNE
                CurrencyId.GACHA_TICKET_NORMAL -> StatKey.FORTUNE
                CurrencyId.GACHA_TICKET_RARE -> StatKey.FORTUNE
                CurrencyId.GACHA_TICKET_SR -> StatKey.FORTUNE
                CurrencyId.GACHA_TICKET_SSR -> StatKey.FORTUNE
            },
            oldValue = oldBalance,
            newValue = newBalance,
            delta = newBalance - oldBalance,
            reason = reason,
            source = source,
            timestamp = System.currentTimeMillis()
        )
        
        _entries.value = _entries.value + entry
    }
    
    fun getEntriesByType(key: StatKey): List<LedgerEntry> {
        return _entries.value.filter { it.key == key }
    }
    
    fun getEntriesBySource(source: ChangeSource): List<LedgerEntry> {
        return _entries.value.filter { it.source == source }
    }
    
    fun getEntriesByTimeRange(startTime: Long, endTime: Long): List<LedgerEntry> {
        return _entries.value.filter { 
            it.timestamp >= startTime && it.timestamp <= endTime 
        }
    }
    
    fun getRecentEntries(count: Int): List<LedgerEntry> {
        return _entries.value.takeLast(count)
    }
    
    fun getTotalDelta(key: StatKey): Int {
        return _entries.value
            .filter { it.key == key }
            .sumOf { it.delta }
    }
    
    fun getNetChange(key: StatKey, startTime: Long, endTime: Long): Int {
        return _entries.value
            .filter { it.key == key && it.timestamp >= startTime && it.timestamp <= endTime }
            .sumOf { it.delta }
    }
    
    fun clearEntries() {
        _entries.value = emptyList()
    }
    
    fun clearEntriesBefore(timestamp: Long) {
        _entries.value = _entries.value.filter { it.timestamp >= timestamp }
    }
    
    private fun generateEntryId(): String {
        return "entry_${System.currentTimeMillis()}_${_entries.value.size}"
    }
    
    private fun getAttributeValue(state: HostState, key: StatKey): Int {
        return when (key) {
            StatKey.FORTUNE -> state.fortune
            StatKey.VITALITY -> state.vitality
            StatKey.MOOD -> state.mood
            StatKey.BOND -> state.bond
            StatKey.FOCUS -> state.focus
        }
    }
}

@kotlinx.serialization.Serializable
data class LedgerEntry(
    val id: String,
    val key: StatKey,
    val oldValue: Int,
    val newValue: Int,
    val delta: Int,
    val reason: String,
    val source: ChangeSource,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long
)

enum class ChangeSource {
    UNKNOWN,
    TASK,
    REWARD,
    BUFF,
    TRANSACTION,
    DECAY,
    ROLLOVER,
    ADMIN,
    SYNC
}
