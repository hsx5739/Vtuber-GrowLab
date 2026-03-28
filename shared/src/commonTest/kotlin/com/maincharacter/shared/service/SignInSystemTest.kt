package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SignInStateTest {
    
    @Test
    fun testIsSignedToday() {
        val state = SignInState(lastSignInDate = "2024-03-27")
        assertTrue(state.isSignedToday())
        
        val state2 = SignInState(lastSignInDate = "2024-03-26")
        assertFalse(state2.isSignedToday())
    }
    
    @Test
    fun testIsSignedInMonth() {
        val state = SignInState(
            currentMonth = "2024-03",
            monthSignedBits = 0b101L
        )
        
        assertTrue(state.isSignedInMonth("2024-03-01"))
        assertFalse(state.isSignedInMonth("2024-03-02"))
        assertTrue(state.isSignedInMonth("2024-03-03"))
    }
    
    @Test
    fun testGetSignedDaysInMonth() {
        val state = SignInState(monthSignedBits = 0b10101L)
        assertEquals(3, state.getSignedDaysInMonth())
    }
    
    @Test
    fun testHasReachedMilestone() {
        val state = SignInState(monthlyMilestones = setOf(7, 14))
        
        assertTrue(state.hasReachedMilestone(7))
        assertTrue(state.hasReachedMilestone(14))
        assertFalse(state.hasReachedMilestone(21))
    }
    
    @Test
    fun testGetStreakBonusMultiplier() {
        val state1 = SignInState(streak = 0)
        assertEquals(1.0f, state1.getStreakBonusMultiplier())
        
        val state2 = SignInState(streak = 5)
        assertEquals(1.0f, state2.getStreakBonusMultiplier())
        
        val state3 = SignInState(streak = 7)
        assertEquals(1.1f, state3.getStreakBonusMultiplier())
        
        val state4 = SignInState(streak = 14)
        assertEquals(1.2f, state4.getStreakBonusMultiplier())
        
        val state5 = SignInState(streak = 21)
        assertEquals(1.3f, state5.getStreakBonusMultiplier())
        
        val state6 = SignInState(streak = 30)
        assertEquals(1.5f, state6.getStreakBonusMultiplier())
    }
    
    @Test
    fun testGetStreakLevel() {
        val state1 = SignInState(streak = 0)
        assertEquals(StreakLevel.NONE, state1.getStreakLevel())
        
        val state2 = SignInState(streak = 3)
        assertEquals(StreakLevel.IRON, state2.getStreakLevel())
        
        val state3 = SignInState(streak = 7)
        assertEquals(StreakLevel.BRONZE, state3.getStreakLevel())
        
        val state4 = SignInState(streak = 14)
        assertEquals(StreakLevel.SILVER, state4.getStreakLevel())
        
        val state5 = SignInState(streak = 21)
        assertEquals(StreakLevel.GOLD, state5.getStreakLevel())
        
        val state6 = SignInState(streak = 30)
        assertEquals(StreakLevel.LEGENDARY, state6.getStreakLevel())
    }
    
    @Test
    fun testIsSpecialDay() {
        val state = SignInState()
        
        assertTrue(state.isSpecialDay("2024-01-01"))
        assertTrue(state.isSpecialDay("2024-02-14"))
        assertTrue(state.isSpecialDay("2024-12-25"))
        assertTrue(state.isSpecialDay("2024-03-30"))
        assertFalse(state.isSpecialDay("2024-03-27"))
    }
    
    @Test
    fun testGetSpecialDayBonus() {
        val state = SignInState()
        
        assertEquals(2.0f, state.getSpecialDayBonus("2024-01-01"))
        assertEquals(1.0f, state.getSpecialDayBonus("2024-03-27"))
    }
    
    @Test
    fun testGetTotalBonusMultiplier() {
        val state = SignInState(streak = 7)
        
        val multiplier1 = state.getTotalBonusMultiplier("2024-03-27")
        assertEquals(1.1f, multiplier1)
        
        val multiplier2 = state.getTotalBonusMultiplier("2024-01-01")
        assertEquals(2.2f, multiplier2)
    }
    
    @Test
    fun testCopyWithSignIn_NewSignIn() {
        val state = SignInState(lastSignInDate = "", streak = 0, totalSignIns = 0)
        val newState = state.copyWithSignIn("2024-03-27")
        
        assertEquals("2024-03-27", newState.lastSignInDate)
        assertEquals(1, newState.streak)
        assertEquals(1, newState.totalSignIns)
    }
    
    @Test
    fun testCopyWithSignIn_ContinueStreak() {
        val state = SignInState(lastSignInDate = "2024-03-26", streak = 5, totalSignIns = 5)
        val newState = state.copyWithSignIn("2024-03-27")
        
        assertEquals("2024-03-27", newState.lastSignInDate)
        assertEquals(6, newState.streak)
        assertEquals(6, newState.totalSignIns)
    }
    
    @Test
    fun testCopyWithSignIn_ResetStreak() {
        val state = SignInState(lastSignInDate = "2024-03-25", streak = 5, totalSignIns = 5)
        val newState = state.copyWithSignIn("2024-03-27")
        
        assertEquals("2024-03-27", newState.lastSignInDate)
        assertEquals(1, newState.streak)
        assertEquals(6, newState.totalSignIns)
    }
    
    @Test
    fun testResetForNewMonth() {
        val state = SignInState(
            currentMonth = "2024-03",
            monthSignedBits = 0b101L,
            monthlyMilestones = setOf(7, 14)
        )
        
        val newState = state.resetForNewMonth()
        
        assertEquals(0L, newState.monthSignedBits)
        assertEquals(emptySet<Int>(), newState.monthlyMilestones)
    }
    
    @Test
    fun testResetStreak() {
        val state = SignInState(streak = 10, monthlyMilestones = setOf(7, 14))
        val newState = state.resetStreak()
        
        assertEquals(0, newState.streak)
        assertEquals(emptySet<Int>(), newState.monthlyMilestones)
    }
}

class SignInRewardTest {
    
    @Test
    fun testCalculateTotalReward_BaseOnly() {
        val baseReward = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 100)
        )
        
        val signInReward = SignInReward(
            baseReward = baseReward,
            streakBonus = null,
            monthlyMilestoneReward = null,
            specialDayReward = null
        )
        
        val totalReward = signInReward.calculateTotalReward(
            streak = 0,
            hasMilestone = false,
            isSpecialDay = false
        )
        
        assertEquals(100, totalReward.currencies[CurrencyId.STAR_DUST])
    }
    
    @Test
    fun testCalculateTotalReward_WithStreakBonus() {
        val baseReward = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 100)
        )
        
        val streakBonus = RewardBundle(
            currencies = mapOf(CurrencyId.MOON_GLOW to 10)
        )
        
        val signInReward = SignInReward(
            baseReward = baseReward,
            streakBonus = streakBonus,
            monthlyMilestoneReward = null,
            specialDayReward = null
        )
        
        val totalReward = signInReward.calculateTotalReward(
            streak = 7,
            hasMilestone = false,
            isSpecialDay = false
        )
        
        assertEquals(100, totalReward.currencies[CurrencyId.STAR_DUST])
        assertEquals(10, totalReward.currencies[CurrencyId.MOON_GLOW])
    }
    
    @Test
    fun testCalculateTotalReward_WithMilestone() {
        val baseReward = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 100)
        )
        
        val milestoneReward = RewardBundle(
            currencies = mapOf(CurrencyId.GACHA_TICKET_NORMAL to 1)
        )
        
        val signInReward = SignInReward(
            baseReward = baseReward,
            streakBonus = null,
            monthlyMilestoneReward = milestoneReward,
            specialDayReward = null
        )
        
        val totalReward = signInReward.calculateTotalReward(
            streak = 7,
            hasMilestone = true,
            isSpecialDay = false
        )
        
        assertEquals(100, totalReward.currencies[CurrencyId.STAR_DUST])
        assertEquals(1, totalReward.currencies[CurrencyId.GACHA_TICKET_NORMAL])
    }
    
    @Test
    fun testCalculateTotalReward_WithSpecialDay() {
        val baseReward = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 100)
        )
        
        val specialDayReward = RewardBundle(
            currencies = mapOf(CurrencyId.MOON_GLOW to 50)
        )
        
        val signInReward = SignInReward(
            baseReward = baseReward,
            streakBonus = null,
            monthlyMilestoneReward = null,
            specialDayReward = specialDayReward
        )
        
        val totalReward = signInReward.calculateTotalReward(
            streak = 0,
            hasMilestone = false,
            isSpecialDay = true
        )
        
        assertEquals(100, totalReward.currencies[CurrencyId.STAR_DUST])
        assertEquals(50, totalReward.currencies[CurrencyId.MOON_GLOW])
    }
    
    @Test
    fun testCalculateTotalReward_AllBonuses() {
        val baseReward = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 100)
        )
        
        val streakBonus = RewardBundle(
            currencies = mapOf(CurrencyId.MOON_GLOW to 10)
        )
        
        val milestoneReward = RewardBundle(
            currencies = mapOf(CurrencyId.GACHA_TICKET_NORMAL to 1)
        )
        
        val specialDayReward = RewardBundle(
            currencies = mapOf(CurrencyId.MOON_GLOW to 50)
        )
        
        val signInReward = SignInReward(
            baseReward = baseReward,
            streakBonus = streakBonus,
            monthlyMilestoneReward = milestoneReward,
            specialDayReward = specialDayReward
        )
        
        val totalReward = signInReward.calculateTotalReward(
            streak = 7,
            hasMilestone = true,
            isSpecialDay = true
        )
        
        assertEquals(100, totalReward.currencies[CurrencyId.STAR_DUST])
        assertEquals(60, totalReward.currencies[CurrencyId.MOON_GLOW])
        assertEquals(1, totalReward.currencies[CurrencyId.GACHA_TICKET_NORMAL])
    }
}

class SignInConfigTest {
    
    @Test
    fun testSignInConfigDefaults() {
        val config = SignInConfig()
        
        assertEquals("UTC", config.timeZone)
        assertEquals(0, config.resetHour)
        assertEquals(listOf(7, 14, 21, 30), config.streakMilestones)
        assertTrue(config.specialDays.isEmpty())
        assertTrue(config.streakResetOnMiss)
        assertFalse(config.bondPenaltyOnMiss)
        assertEquals(0, config.bondPenaltyAmount)
    }
    
    @Test
    fun testSignInConfigCustom() {
        val config = SignInConfig(
            timeZone = "Asia/Shanghai",
            resetHour = 4,
            streakMilestones = listOf(5, 10, 15),
            streakResetOnMiss = false
        )
        
        assertEquals("Asia/Shanghai", config.timeZone)
        assertEquals(4, config.resetHour)
        assertEquals(listOf(5, 10, 15), config.streakMilestones)
        assertFalse(config.streakResetOnMiss)
    }
}

class StreakLevelTest {
    
    @Test
    fun testStreakLevelValues() {
        val levels = StreakLevel.values()
        assertEquals(6, levels.size)
        assertTrue(levels.contains(StreakLevel.NONE))
        assertTrue(levels.contains(StreakLevel.IRON))
        assertTrue(levels.contains(StreakLevel.BRONZE))
        assertTrue(levels.contains(StreakLevel.SILVER))
        assertTrue(levels.contains(StreakLevel.GOLD))
        assertTrue(levels.contains(StreakLevel.LEGENDARY))
    }
}