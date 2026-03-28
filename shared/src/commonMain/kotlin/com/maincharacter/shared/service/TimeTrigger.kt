package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class TimeTrigger(
    private val attributeCalculator: AttributeCalculator,
    private val buffManager: BuffManager
) {
    
    private val _lastDayRollover = MutableStateFlow<LocalDate?>(null)
    val lastDayRollover: StateFlow<LocalDate?> = _lastDayRollover
    
    private val _lastWeekRollover = MutableStateFlow<LocalDate?>(null)
    val lastWeekRollover: StateFlow<LocalDate?> = _lastWeekRollover
    
    private val _triggerEvents = MutableStateFlow<List<TriggerEvent>>(emptyList())
    val triggerEvents: StateFlow<List<TriggerEvent>> = _triggerEvents
    
    fun checkDayRollover(currentState: HostState): HostState {
        val today = LocalDate.now()
        val lastRollover = _lastDayRollover.value
        
        if (lastRollover != null && lastRollover.isEqual(today)) {
            return currentState
        }
        
        val updatedState = onDayRollover(currentState, today)
        _lastDayRollover.value = today
        
        return updatedState
    }
    
    fun checkWeekRollover(currentState: HostState): HostState {
        val today = LocalDate.now()
        val weekStart = getWeekStart(today)
        val lastRollover = _lastWeekRollover.value
        
        if (lastRollover != null && lastRollover.isEqual(weekStart)) {
            return currentState
        }
        
        val updatedState = onWeekRollover(currentState, weekStart)
        _lastWeekRollover.value = weekStart
        
        return updatedState
    }
    
    private fun onDayRollover(
        currentState: HostState,
        today: LocalDate
    ): HostState {
        var state = currentState
        
        state = buffManager.cleanExpiredBuffs(state)
        
        state = state.copy(
            dailyTaskCompletionsByType = emptyMap(),
            dailyVerifiedTaskCount = 0
        )
        
        state = applyDailyDecay(state)
        
        _triggerEvents.value = _triggerEvents.value + TriggerEvent(
            type = TriggerType.DAY_ROLLOVER,
            timestamp = System.currentTimeMillis(),
            data = mapOf("date" to today.toString())
        )
        
        return state
    }
    
    private fun onWeekRollover(
        currentState: HostState,
        weekStart: LocalDate
    ): HostState {
        var state = currentState
        
        state = applyWeeklyReset(state)
        
        state = applyWeeklyDecay(state)
        
        _triggerEvents.value = _triggerEvents.value + TriggerEvent(
            type = TriggerType.WEEK_ROLLOVER,
            timestamp = System.currentTimeMillis(),
            data = mapOf("weekStart" to weekStart.toString())
        )
        
        return state
    }
    
    private fun applyDailyDecay(state: HostState): HostState {
        val decayDeltas = listOf(
            StatDelta(StatKey.VITALITY, -5),
            StatDelta(StatKey.MOOD, -3)
        )
        
        return attributeCalculator.applyDeltas(state, decayDeltas, "daily_decay")
    }
    
    private fun applyWeeklyDecay(state: HostState): HostState {
        val decayDeltas = listOf(
            StatDelta(StatKey.FORTUNE, -2),
            StatDelta(StatKey.BOND, -1)
        )
        
        return attributeCalculator.applyDeltas(state, decayDeltas, "weekly_decay")
    }
    
    private fun applyWeeklyReset(state: HostState): HostState {
        return state.copy(
            verificationScore = 0
        )
    }
    
    fun forceDayRollover(currentState: HostState): HostState {
        val today = LocalDate.now()
        val updatedState = onDayRollover(currentState, today)
        _lastDayRollover.value = today
        return updatedState
    }
    
    fun forceWeekRollover(currentState: HostState): HostState {
        val weekStart = getWeekStart(LocalDate.now())
        val updatedState = onWeekRollover(currentState, weekStart)
        _lastWeekRollover.value = weekStart
        return updatedState
    }
    
    fun getDaysUntilNextRollover(): Long {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val now = LocalDateTime.now()
        val midnight = tomorrow.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()
        return java.time.Duration.between(now, midnight).toMinutes()
    }
    
    fun getWeeksUntilNextRollover(): Long {
        val today = LocalDate.now()
        val weekStart = getWeekStart(today)
        val nextWeekStart = weekStart.plusWeeks(1)
        val now = LocalDateTime.now()
        val nextWeekMidnight = nextWeekStart.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()
        return java.time.Duration.between(now, nextWeekMidnight).toDays() / 7
    }
    
    private fun getWeekStart(date: LocalDate): LocalDate {
        return date.minusDays(date.dayOfWeek.value.toLong() - 1)
    }
    
    fun clearEvents() {
        _triggerEvents.value = emptyList()
    }
    
    fun getEvents(): List<TriggerEvent> {
        return _triggerEvents.value
    }
}

data class TriggerEvent(
    val type: TriggerType,
    val timestamp: Long,
    val data: Map<String, String> = emptyMap()
)

enum class TriggerType {
    DAY_ROLLOVER,
    WEEK_ROLLOVER
}