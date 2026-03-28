package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SkillSystemTest {
    
    @Test
    fun testSkillCardUsage() {
        val skillManager = TestSkillManager()
        val userId = "user_skill_001"
        
        val skillId = "skill_barrier"
        val skill = skillManager.getSkill(skillId)
        assertNotNull(skill)
        assertEquals("屏障", skill.name)
        assertEquals(SkillType.ACTIVE, skill.type)
        assertEquals(30, skill.cooldownSeconds)
        
        val result = skillManager.useSkill(userId, skillId)
        assertTrue(result.success)
        assertEquals(skillId, result.skillId)
        assertEquals(30, result.cooldownRemaining)
    }
    
    @Test
    fun testSkillCooldown() {
        val skillManager = TestSkillManager()
        val userId = "user_skill_002"
        val skillId = "skill_heal"
        
        val skill = skillManager.getSkill(skillId)
        assertNotNull(skill)
        assertEquals(60, skill.cooldownSeconds)
        
        val result1 = skillManager.useSkill(userId, skillId)
        assertTrue(result1.success)
        assertEquals(60, result1.cooldownRemaining)
        
        val result2 = skillManager.useSkill(userId, skillId)
        assertFalse(result2.success)
        assertEquals(60, result2.cooldownRemaining)
        
        val cooldownStatus = skillManager.getCooldownStatus(userId, skillId)
        assertTrue(cooldownStatus.isOnCooldown)
        assertEquals(60, cooldownStatus.remainingSeconds)
    }
    
    @Test
    fun testCooldownReduction() {
        val skillManager = TestSkillManager()
        val userId = "user_skill_003"
        val skillId = "skill_boost"
        
        val skill = skillManager.getSkill(skillId)
        assertNotNull(skill)
        assertEquals(45, skill.cooldownSeconds)
        
        val result1 = skillManager.useSkill(userId, skillId)
        assertTrue(result1.success)
        assertEquals(45, result1.cooldownRemaining)
        
        skillManager.reduceCooldown(userId, skillId, 15)
        
        val cooldownStatus = skillManager.getCooldownStatus(userId, skillId)
        assertTrue(cooldownStatus.isOnCooldown)
        assertEquals(30, cooldownStatus.remainingSeconds)
        
        skillManager.reduceCooldown(userId, skillId, 30)
        
        val finalStatus = skillManager.getCooldownStatus(userId, skillId)
        assertFalse(finalStatus.isOnCooldown)
        assertEquals(0, finalStatus.remainingSeconds)
    }
    
    @Test
    fun testMultipleSkillsCooldown() {
        val skillManager = TestSkillManager()
        val userId = "user_skill_004"
        
        val skill1Id = "skill_barrier"
        val skill2Id = "skill_heal"
        val skill3Id = "skill_boost"
        
        val result1 = skillManager.useSkill(userId, skill1Id)
        assertTrue(result1.success)
        
        val result2 = skillManager.useSkill(userId, skill2Id)
        assertTrue(result2.success)
        
        val result3 = skillManager.useSkill(userId, skill3Id)
        assertTrue(result3.success)
        
        val status1 = skillManager.getCooldownStatus(userId, skill1Id)
        assertTrue(status1.isOnCooldown)
        assertEquals(30, status1.remainingSeconds)
        
        val status2 = skillManager.getCooldownStatus(userId, skill2Id)
        assertTrue(status2.isOnCooldown)
        assertEquals(60, status2.remainingSeconds)
        
        val status3 = skillManager.getCooldownStatus(userId, skill3Id)
        assertTrue(status3.isOnCooldown)
        assertEquals(45, status3.remainingSeconds)
    }
    
    @Test
    fun testSkillEffect() {
        val skillManager = TestSkillManager()
        val userId = "user_skill_005"
        val skillId = "skill_heal"
        
        val initialHealth = 80
        val hostState = HostState(
            fortune = 100,
            vitality = initialHealth,
            mood = 100,
            bond = 50,
            focus = 100
        )
        
        val result = skillManager.useSkill(userId, skillId, hostState)
        assertTrue(result.success)
        assertNotNull(result.effect)
        
        val finalHealth = result.effect!!.appliedDeltas.find { it.stat == "vitality" }?.value
        assertNotNull(finalHealth)
        assertTrue(finalHealth > 0)
    }
    
    @Test
    fun testSkillCost() {
        val skillManager = TestSkillManager()
        val userId = "user_skill_006"
        val skillId = "skill_barrier"
        
        val skill = skillManager.getSkill(skillId)
        assertNotNull(skill)
        assertEquals(20, skill.energyCost)
        
        val initialEnergy = 100
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = initialEnergy
        )
        
        val result = skillManager.useSkill(userId, skillId, hostState)
        assertTrue(result.success)
        assertEquals(initialEnergy - skill.energyCost, result.finalEnergy)
    }
    
    @Test
    fun testInsufficientEnergy() {
        val skillManager = TestSkillManager()
        val userId = "user_skill_007"
        val skillId = "skill_barrier"
        
        val skill = skillManager.getSkill(skillId)
        assertNotNull(skill)
        assertEquals(20, skill.energyCost)
        
        val lowEnergy = 10
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = lowEnergy
        )
        
        val result = skillManager.useSkill(userId, skillId, hostState)
        assertFalse(result.success)
        assertEquals("Insufficient energy", result.errorMessage)
    }
    
    @Test
    fun testCooldownReset() {
        val skillManager = TestSkillManager()
        val userId = "user_skill_008"
        val skillId = "skill_heal"
        
        val result1 = skillManager.useSkill(userId, skillId)
        assertTrue(result1.success)
        
        val status1 = skillManager.getCooldownStatus(userId, skillId)
        assertTrue(status1.isOnCooldown)
        
        skillManager.resetCooldown(userId, skillId)
        
        val status2 = skillManager.getCooldownStatus(userId, skillId)
        assertFalse(status2.isOnCooldown)
        assertEquals(0, status2.remainingSeconds)
    }
    
    @Test
    fun testSkillUsageHistory() {
        val skillManager = TestSkillManager()
        val userId = "user_skill_009"
        val skillId = "skill_barrier"
        
        val result1 = skillManager.useSkill(userId, skillId)
        assertTrue(result1.success)
        
        val history1 = skillManager.getUsageHistory(userId, skillId)
        assertEquals(1, history1.size)
        assertEquals(skillId, history1[0].skillId)
        
        skillManager.reduceCooldown(userId, skillId, 30)
        
        val result2 = skillManager.useSkill(userId, skillId)
        assertTrue(result2.success)
        
        val history2 = skillManager.getUsageHistory(userId, skillId)
        assertEquals(2, history2.size)
    }
    
    @Test
    fun testPassiveSkillEffect() {
        val skillManager = TestSkillManager()
        val userId = "user_skill_010"
        val passiveSkillId = "passive_bond_boost"
        
        val skill = skillManager.getSkill(passiveSkillId)
        assertNotNull(skill)
        assertEquals(SkillType.PASSIVE, skill.type)
        
        val initialBond = 50
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = initialBond,
            focus = 100
        )
        
        val effect = skillManager.applyPassiveSkill(userId, passiveSkillId, hostState)
        assertNotNull(effect)
        
        val bondDelta = effect.appliedDeltas.find { it.stat == "bond" }?.value
        assertNotNull(bondDelta)
        assertTrue(bondDelta > 0)
    }
}

class TestSkillManager : SkillManager {
    private val _skills = MutableStateFlow<Map<String, Skill>>(emptyMap())
    override val skills: StateFlow<Map<String, Skill>> = _skills
    
    private val _cooldowns = MutableStateFlow<Map<String, Map<String, Long>>>(emptyMap())
    private val _usageHistory = MutableStateFlow<Map<String, List<SkillUsageRecord>>>(emptyMap())
    
    init {
        val testSkills = mapOf(
            "skill_barrier" to Skill(
                id = "skill_barrier",
                name = "屏障",
                type = SkillType.ACTIVE,
                cooldownSeconds = 30,
                energyCost = 20,
                description = "创建一个保护屏障",
                effect = SkillEffect(
                    stat = "vitality",
                    value = 10,
                    duration = 60
                )
            ),
            "skill_heal" to Skill(
                id = "skill_heal",
                name = "治愈",
                type = SkillType.ACTIVE,
                cooldownSeconds = 60,
                energyCost = 15,
                description = "恢复生命值",
                effect = SkillEffect(
                    stat = "vitality",
                    value = 20,
                    duration = 0
                )
            ),
            "skill_boost" to Skill(
                id = "skill_boost",
                name = "强化",
                type = SkillType.ACTIVE,
                cooldownSeconds = 45,
                energyCost = 25,
                description = "提升属性",
                effect = SkillEffect(
                    stat = "fortune",
                    value = 15,
                    duration = 120
                )
            ),
            "passive_bond_boost" to Skill(
                id = "passive_bond_boost",
                name = "羁绊强化",
                type = SkillType.PASSIVE,
                cooldownSeconds = 0,
                energyCost = 0,
                description = "提升羁绊值",
                effect = SkillEffect(
                    stat = "bond",
                    value = 5,
                    duration = 0
                )
            )
        )
        _skills.value = testSkills
    }
    
    override fun getSkill(skillId: String): Skill? {
        return _skills.value[skillId]
    }
    
    override fun useSkill(userId: String, skillId: String): SkillUsageResult {
        val skill = getSkill(skillId) ?: return SkillUsageResult(
            success = false,
            skillId = skillId,
            errorMessage = "Skill not found"
        )
        
        val userCooldowns = _cooldowns.value[userId] ?: emptyMap()
        val lastUsed = userCooldowns[skillId] ?: 0
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastUsed < skill.cooldownSeconds * 1000) {
            val remaining = skill.cooldownSeconds - ((currentTime - lastUsed) / 1000).toInt()
            return SkillUsageResult(
                success = false,
                skillId = skillId,
                cooldownRemaining = remaining,
                errorMessage = "Skill is on cooldown"
            )
        }
        
        val updatedCooldowns = userCooldowns.toMutableMap()
        updatedCooldowns[skillId] = currentTime
        _cooldowns.value = _cooldowns.value.toMutableMap().apply {
            this[userId] = updatedCooldowns
        }
        
        val history = _usageHistory.value[userId]?.toMutableList() ?: mutableListOf()
        history.add(SkillUsageRecord(
            skillId = skillId,
            timestamp = currentTime
        ))
        _usageHistory.value = _usageHistory.value.toMutableMap().apply {
            this[userId] = history
        }
        
        return SkillUsageResult(
            success = true,
            skillId = skillId,
            cooldownRemaining = skill.cooldownSeconds
        )
    }
    
    override fun useSkill(userId: String, skillId: String, hostState: HostState): SkillUsageResult {
        val skill = getSkill(skillId) ?: return SkillUsageResult(
            success = false,
            skillId = skillId,
            errorMessage = "Skill not found"
        )
        
        if (hostState.focus < skill.energyCost) {
            return SkillUsageResult(
                success = false,
                skillId = skillId,
                errorMessage = "Insufficient energy"
            )
        }
        
        val result = useSkill(userId, skillId)
        if (!result.success) {
            return result
        }
        
        val effect = SkillEffectResult(
            skillId = skillId,
            appliedDeltas = listOf(StatDelta(skill.effect.stat, skill.effect.value))
        )
        
        return result.copy(
            effect = effect,
            finalEnergy = hostState.focus - skill.energyCost
        )
    }
    
    override fun getCooldownStatus(userId: String, skillId: String): CooldownStatus {
        val skill = getSkill(skillId) ?: return CooldownStatus(
            isOnCooldown = false,
            remainingSeconds = 0
        )
        
        val userCooldowns = _cooldowns.value[userId] ?: emptyMap()
        val lastUsed = userCooldowns[skillId] ?: 0
        val currentTime = System.currentTimeMillis()
        
        val elapsed = (currentTime - lastUsed) / 1000
        val remaining = skill.cooldownSeconds - elapsed.toInt()
        
        return CooldownStatus(
            isOnCooldown = remaining > 0,
            remainingSeconds = if (remaining > 0) remaining else 0
        )
    }
    
    override fun reduceCooldown(userId: String, skillId: String, seconds: Int) {
        val userCooldowns = _cooldowns.value[userId] ?: emptyMap()
        val lastUsed = userCooldowns[skillId] ?: return
        
        val reducedTime = lastUsed - (seconds * 1000)
        val updatedCooldowns = userCooldowns.toMutableMap()
        updatedCooldowns[skillId] = reducedTime
        _cooldowns.value = _cooldowns.value.toMutableMap().apply {
            this[userId] = updatedCooldowns
        }
    }
    
    override fun resetCooldown(userId: String, skillId: String) {
        val userCooldowns = _cooldowns.value[userId] ?: return
        val updatedCooldowns = userCooldowns.toMutableMap()
        updatedCooldowns.remove(skillId)
        _cooldowns.value = _cooldowns.value.toMutableMap().apply {
            this[userId] = updatedCooldowns
        }
    }
    
    override fun getUsageHistory(userId: String, skillId: String): List<SkillUsageRecord> {
        val userHistory = _usageHistory.value[userId] ?: emptyList()
        return userHistory.filter { it.skillId == skillId }
    }
    
    override fun applyPassiveSkill(userId: String, skillId: String, hostState: HostState): SkillEffectResult? {
        val skill = getSkill(skillId) ?: return null
        
        return SkillEffectResult(
            skillId = skillId,
            appliedDeltas = listOf(StatDelta(skill.effect.stat, skill.effect.value))
        )
    }
}

interface SkillManager {
    val skills: StateFlow<Map<String, Skill>>
    
    fun getSkill(skillId: String): Skill?
    fun useSkill(userId: String, skillId: String): SkillUsageResult
    fun useSkill(userId: String, skillId: String, hostState: HostState): SkillUsageResult
    fun getCooldownStatus(userId: String, skillId: String): CooldownStatus
    fun reduceCooldown(userId: String, skillId: String, seconds: Int)
    fun resetCooldown(userId: String, skillId: String)
    fun getUsageHistory(userId: String, skillId: String): List<SkillUsageRecord>
    fun applyPassiveSkill(userId: String, skillId: String, hostState: HostState): SkillEffectResult?
}

data class Skill(
    val id: String,
    val name: String,
    val type: SkillType,
    val cooldownSeconds: Int,
    val energyCost: Int,
    val description: String,
    val effect: SkillEffect
)

enum class SkillType {
    ACTIVE,
    PASSIVE
}

data class SkillEffect(
    val stat: String,
    val value: Int,
    val duration: Int
)

data class SkillUsageResult(
    val success: Boolean,
    val skillId: String,
    val cooldownRemaining: Int = 0,
    val errorMessage: String? = null,
    val effect: SkillEffectResult? = null,
    val finalEnergy: Int? = null
)

data class CooldownStatus(
    val isOnCooldown: Boolean,
    val remainingSeconds: Int
)

data class SkillEffectResult(
    val skillId: String,
    val appliedDeltas: List<StatDelta>
)

data class SkillUsageRecord(
    val skillId: String,
    val timestamp: Long
)
