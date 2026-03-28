package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class SignInState(
    val lastSignInDate: String = "",
    val streak: Int = 0,
    val totalSignIns: Int = 0,
    val monthSignedBits: Long = 0L,
    val currentMonth: String = "",
    val lastMonthSignedBits: Long = 0L,
    val lastMonth: String = "",
    val specialDaysSigned: Set<String> = emptySet(),
    val monthlyMilestones: Set<Int> = emptySet(),
    val timeZone: String = "UTC"
) {
    fun isSignedToday(): Boolean {
        return lastSignInDate == getCurrentDate()
    }
    
    fun isSignedInMonth(date: String): Boolean {
        val monthKey = date.substring(0, 7)
        return if (monthKey == currentMonth) {
            isSignedInCurrentMonth(date)
        } else if (monthKey == lastMonth) {
            isSignedInLastMonth(date)
        } else {
            false
        }
    }
    
    private fun isSignedInCurrentMonth(date: String): Boolean {
        val dayOfMonth = date.substring(8, 10).toInt()
        val bitPosition = dayOfMonth - 1
        return (monthSignedBits and (1L shl bitPosition)) != 0L
    }
    
    private fun isSignedInLastMonth(date: String): Boolean {
        val dayOfMonth = date.substring(8, 10).toInt()
        val bitPosition = dayOfMonth - 1
        return (lastMonthSignedBits and (1L shl bitPosition)) != 0L
    }
    
    fun getSignedDaysInMonth(): Int {
        return java.lang.Long.bitCount(monthSignedBits)
    }
    
    fun getSignedDaysInLastMonth(): Int {
        return java.lang.Long.bitCount(lastMonthSignedBits)
    }
    
    fun hasReachedMilestone(milestone: Int): Boolean {
        return monthlyMilestones.contains(milestone)
    }
    
    fun isNewMonth(): Boolean {
        val currentMonthKey = getCurrentMonthKey()
        return currentMonthKey != currentMonth
    }
    
    fun getStreakBonusMultiplier(): Float {
        return when {
            streak >= 30 -> 1.5f
            streak >= 21 -> 1.3f
            streak >= 14 -> 1.2f
            streak >= 7 -> 1.1f
            else -> 1.0f
        }
    }
    
    fun getStreakLevel(): StreakLevel {
        return when {
            streak >= 30 -> StreakLevel.LEGENDARY
            streak >= 21 -> StreakLevel.GOLD
            streak >= 14 -> StreakLevel.SILVER
            streak >= 7 -> StreakLevel.BRONZE
            streak >= 3 -> StreakLevel.IRON
            else -> StreakLevel.NONE
        }
    }
    
    fun isSpecialDay(date: String): Boolean {
        val month = date.substring(5, 7).toInt()
        val day = date.substring(8, 10).toInt()
        
        return when {
            month == 1 && day == 1 -> true
            month == 2 && day == 14 -> true
            month == 12 && day == 25 -> true
            day % 7 == 0 -> true
            else -> false
        }
    }
    
    fun getSpecialDayBonus(date: String): Float {
        return if (isSpecialDay(date)) {
            2.0f
        } else {
            1.0f
        }
    }
    
    fun getTotalBonusMultiplier(date: String): Float {
        return getStreakBonusMultiplier() * getSpecialDayBonus(date)
    }
    
    private fun getCurrentDate(): String {
        return java.time.LocalDate.now().toString()
    }
    
    private fun getCurrentMonthKey(): String {
        return getCurrentDate().substring(0, 7)
    }
    
    fun copyWithSignIn(date: String): SignInState {
        val newStreak = calculateNewStreak(date)
        val newTotalSignIns = totalSignIns + 1
        val newMonthSignedBits = updateMonthSignedBits(date)
        val newSpecialDaysSigned = if (isSpecialDay(date)) {
            specialDaysSigned + date
        } else {
            specialDaysSigned
        }
        val newMonthlyMilestones = updateMonthlyMilestones(newStreak)
        
        return copy(
            lastSignInDate = date,
            streak = newStreak,
            totalSignIns = newTotalSignIns,
            monthSignedBits = newMonthSignedBits,
            currentMonth = getCurrentMonthKey(),
            lastMonthSignedBits = if (isNewMonth()) monthSignedBits else lastMonthSignedBits,
            lastMonth = if (isNewMonth()) currentMonth else lastMonth,
            specialDaysSigned = newSpecialDaysSigned,
            monthlyMilestones = newMonthlyMilestones
        )
    }
    
    private fun calculateNewStreak(date: String): Int {
        if (lastSignInDate.isEmpty()) {
            return 1
        }
        
        val lastDate = java.time.LocalDate.parse(lastSignInDate)
        val currentDate = java.time.LocalDate.parse(date)
        val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastDate, currentDate).toInt()
        
        return when {
            daysBetween == 0 -> streak
            daysBetween == 1 -> streak + 1
            else -> 1
        }
    }
    
    private fun updateMonthSignedBits(date: String): Long {
        val dayOfMonth = date.substring(8, 10).toInt()
        val bitPosition = dayOfMonth - 1
        return monthSignedBits or (1L shl bitPosition)
    }
    
    private fun updateMonthlyMilestones(newStreak: Int): Set<Int> {
        val milestones = setOf(7, 14, 21, 30)
        val newMilestones = mutableSetOf<Int>()
        
        milestones.forEach { milestone ->
            if (newStreak >= milestone && !monthlyMilestones.contains(milestone)) {
                newMilestones.add(milestone)
            }
        }
        
        return monthlyMilestones + newMilestones
    }
    
    fun resetForNewMonth(): SignInState {
        return copy(
            monthSignedBits = 0L,
            currentMonth = getCurrentMonthKey(),
            lastMonthSignedBits = monthSignedBits,
            lastMonth = currentMonth,
            monthlyMilestones = emptySet()
        )
    }
    
    fun resetStreak(): SignInState {
        return copy(
            streak = 0,
            monthlyMilestones = emptySet()
        )
    }
}

enum class StreakLevel {
    NONE,
    IRON,
    BRONZE,
    SILVER,
    GOLD,
    LEGENDARY
}

@Serializable
data class SignInReward(
    val baseReward: RewardBundle,
    val streakBonus: RewardBundle? = null,
    val monthlyMilestoneReward: RewardBundle? = null,
    val specialDayReward: RewardBundle? = null
) {
    fun calculateTotalReward(
        streak: Int,
        hasMilestone: Boolean,
        isSpecialDay: Boolean
    ): RewardBundle {
        var currencies = baseReward.currencies.toMutableMap()
        var statDelta = baseReward.statDelta.toMutableMap()
        var items = baseReward.items.toMutableList()
        var buffs = baseReward.buffs.toMutableList()
        
        if (streak > 0 && streakBonus != null) {
            currencies = mergeCurrencies(currencies, streakBonus.currencies)
            statDelta = mergeStatDeltas(statDelta, streakBonus.statDelta)
            items.addAll(streakBonus.items)
            buffs.addAll(streakBonus.buffs)
        }
        
        if (hasMilestone && monthlyMilestoneReward != null) {
            currencies = mergeCurrencies(currencies, monthlyMilestoneReward.currencies)
            statDelta = mergeStatDeltas(statDelta, monthlyMilestoneReward.statDelta)
            items.addAll(monthlyMilestoneReward.items)
            buffs.addAll(monthlyMilestoneReward.buffs)
        }
        
        if (isSpecialDay && specialDayReward != null) {
            currencies = mergeCurrencies(currencies, specialDayReward.currencies)
            statDelta = mergeStatDeltas(statDelta, specialDayReward.statDelta)
            items.addAll(specialDayReward.items)
            buffs.addAll(specialDayReward.buffs)
        }
        
        return RewardBundle(
            currencies = currencies,
            statDelta = statDelta,
            items = items,
            buffs = buffs,
            telemetryTags = baseReward.telemetryTags
        )
    }
    
    private fun mergeCurrencies(
        base: Map<CurrencyId, Int>,
        additional: Map<CurrencyId, Int>
    ): MutableMap<CurrencyId, Int> {
        val merged = base.toMutableMap()
        additional.forEach { (currencyId, amount) ->
            merged[currencyId] = (merged[currencyId] ?: 0) + amount
        }
        return merged
    }
    
    private fun mergeStatDeltas(
        base: Map<StatKey, Int>,
        additional: Map<StatKey, Int>
    ): MutableMap<StatKey, Int> {
        val merged = base.toMutableMap()
        additional.forEach { (key, delta) ->
            merged[key] = (merged[key] ?: 0) + delta
        }
        return merged
    }
}

@Serializable
data class SignInConfig(
    val timeZone: String = "UTC",
    val resetHour: Int = 0,
    val streakMilestones: List<Int> = listOf(7, 14, 21, 30),
    val specialDays: List<String> = emptyList(),
    val streakResetOnMiss: Boolean = true,
    val bondPenaltyOnMiss: Boolean = false,
    val bondPenaltyAmount: Int = 0
)
