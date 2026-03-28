package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class SkillDef(
    val id: String,
    val name: String,
    val description: String,
    val type: SkillType,
    val rarity: SkillRarity,
    val category: SkillCategory,
    val effects: List<SkillEffect>,
    val cooldown: SkillCooldown,
    val requirements: SkillRequirements,
    val unlockConditions: SkillUnlockConditions,
    val visualEffects: SkillVisualEffects,
    val metadata: SkillMetadata
) {
    fun hasEffect(effectType: SkillEffectType): Boolean {
        return effects.any { it.type == effectType }
    }
    
    fun getEffect(effectType: SkillEffectType): SkillEffect? {
        return effects.find { it.type == effectType }
    }
    
    fun getEffectsByType(effectType: SkillEffectType): List<SkillEffect> {
        return effects.filter { it.type == effectType }
    }
    
    fun isPassive(): Boolean {
        return type == SkillType.PASSIVE
    }
    
    fun isActive(): Boolean {
        return type == SkillType.ACTIVE
    }
    
    fun isCosmetic(): Boolean {
        return type == SkillType.COSMETIC
    }
    
    fun canUnlock(hostState: HostState): Boolean {
        if (requirements.minBond > hostState.bond) return false
        if (requirements.minFortune > hostState.fortune) return false
        if (requirements.minVitality > hostState.vitality) return false
        if (requirements.minMood > hostState.mood) return false
        
        return true
    }
    
    fun getCooldownMs(): Long {
        return cooldown.baseCooldownMs
    }
    
    fun getModifiedCooldownMs(modifier: Float): Long {
        return (cooldown.baseCooldownMs * (1 - modifier)).toLong().coerceAtLeast(0L)
    }
}

@Serializable
data class SkillEffect(
    val type: SkillEffectType,
    val value: Float,
    val duration: Long,
    val target: EffectTarget,
    val description: String,
    val metadata: Map<String, String>
)

enum class SkillEffectType {
    WEIGHT_MODIFIER,
    PROBABILITY_BOOST,
    COST_REDUCTION,
    CD_REDUCTION,
    ATTRIBUTE_BOOST,
    CURRENCY_BONUS,
    UNLOCK_OPTION,
    EVENT_TRIGGER,
    BUFF_APPLY,
    DEBUFF_REMOVE,
    SKILL_UNLOCK,
    SKIN_UNLOCK,
    DIALOGUE_UNLOCK,
    ANIMATION_UNLOCK
}

enum class EffectTarget {
    SELF,
    EVENT,
    CHOICE,
    OUTCOME,
    GLOBAL
}

@Serializable
data class SkillCooldown(
    val baseCooldownMs: Long,
    val minCooldownMs: Long,
    val maxCooldownMs: Long,
    val charges: Int,
    val chargeRegenTimeMs: Long
)

@Serializable
data class SkillRequirements(
    val minBond: Int,
    val minFortune: Int,
    val minVitality: Int,
    val minMood: Int,
    val requiredSkills: List<String>,
    val requiredItems: List<String>,
    val requiredTasks: List<String>
)

@Serializable
data class SkillUnlockConditions(
    val unlockType: UnlockType,
    val unlockValue: String,
    val unlockCost: UnlockCost,
    val isOneTime: Boolean,
    val isPermanent: Boolean,
    val expirationTime: Long?
)

enum class UnlockType {
    FREE,
    TASK_REWARD,
    EVENT_REWARD,
    GACHA,
    PURCHASE,
    LEVEL_UP,
    ACHIEVEMENT,
    SEASONAL
}

@Serializable
data class UnlockCost(
    val currency: CurrencyType,
    val amount: Int,
    val items: List<ItemCost>
)

@Serializable
data class SkillVisualEffects(
    val iconUrl: String,
    val cardBackgroundUrl: String,
    val animationUrl: String?,
    val unlockAnimationUrl: String?,
    val useAnimationUrl: String?,
    val colorScheme: ColorScheme,
    val particleEffects: List<ParticleEffect>
)

@Serializable
data class ColorScheme(
    val primaryColor: String,
    val secondaryColor: String,
    val accentColor: String,
    val backgroundColor: String
)

@Serializable
data class ParticleEffect(
    val type: ParticleType,
    val color: String,
    val size: Float,
    val count: Int,
    val duration: Long
)

enum class ParticleType {
    SPARKLE,
    GLOW,
    TRAIL,
    EXPLOSION,
    SWIRL,
    RISE
}

@Serializable
data class SkillMetadata(
    val version: String,
    val author: String,
    val createdAt: Long,
    val updatedAt: Long,
    val notes: String,
    val isExperimental: Boolean,
    val isSeasonal: Boolean,
    val seasonInfo: SeasonInfo?
)

enum class SkillType {
    PASSIVE,
    ACTIVE,
    COSMETIC
}

enum class SkillRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}

enum class SkillCategory {
    COMBAT,
    SUPPORT,
    UTILITY,
    SOCIAL,
    LUCK,
    DEFENSE,
    OFFENSE,
    SPECIAL
}

@Serializable
data class SkillInstance(
    val id: String,
    val skillId: String,
    val level: Int,
    val experience: Int,
    val isUnlocked: Boolean,
    val isEquipped: Boolean,
    val unlockTime: Long,
    val lastUseTime: Long,
    val useCount: Int,
    val cooldownEndTime: Long,
    val charges: Int,
    val metadata: SkillInstanceMetadata
) {
    fun isAvailable(): Boolean {
        if (!isUnlocked) return false
        if (cooldownEndTime > System.currentTimeMillis()) return false
        if (charges <= 0) return false
        
        return true
    }
    
    fun getRemainingCooldownMs(): Long {
        val remaining = cooldownEndTime - System.currentTimeMillis()
        return remaining.coerceAtLeast(0L)
    }
    
    fun getRemainingCooldownSeconds(): Float {
        return getRemainingCooldownMs() / 1000f
    }
    
    fun getRemainingCooldownMinutes(): Float {
        return getRemainingCooldownMs() / 60000f
    }
    
    fun getRemainingCooldownHours(): Float {
        return getRemainingCooldownMs() / 3600000f
    }
    
    fun getCooldownProgress(): Float {
        val skillDef = SkillRegistry.getSkillById(skillId) ?: return 0f
        val totalCooldown = skillDef.cooldown.baseCooldownMs
        
        if (totalCooldown == 0L) return 1f
        
        val elapsed = totalCooldown - getRemainingCooldownMs()
        return (elapsed.toFloat() / totalCooldown.toFloat()).coerceIn(0f, 1f)
    }
    
    fun getExperienceToNextLevel(): Int {
        return level * 100
    }
    
    fun getExperienceProgress(): Float {
        val required = getExperienceToNextLevel()
        return if (required > 0) {
            (experience.toFloat() / required.toFloat()) * 100f
        } else {
            100f
        }
    }
    
    fun canUse(): Boolean {
        return isAvailable() && isEquipped
    }
    
    fun addExperience(amount: Int): SkillInstance {
        val newExperience = experience + amount
        val required = getExperienceToNextLevel()
        
        return if (newExperience >= required) {
            val newLevel = level + 1
            val remainingExperience = newExperience - required
            
            copy(
                level = newLevel,
                experience = remainingExperience
            )
        } else {
            copy(experience = newExperience)
        }
    }
    
    fun recordUse(cooldownMs: Long): SkillInstance {
        return copy(
            lastUseTime = System.currentTimeMillis(),
            useCount = useCount + 1,
            cooldownEndTime = System.currentTimeMillis() + cooldownMs,
            charges = (charges - 1).coerceAtLeast(0)
        )
    }
    
    fun regenerateCharge(regenTimeMs: Long): SkillInstance {
        return if (charges < getMaxCharges()) {
            copy(charges = charges + 1)
        } else {
            this
        }
    }
    
    private fun getMaxCharges(): Int {
        val skillDef = SkillRegistry.getSkillById(skillId) ?: return 1
        return skillDef.cooldown.charges
    }
}

@Serializable
data class SkillInstanceMetadata(
    val userId: String,
    val unlockSource: String,
    val customName: String?,
    val customNotes: String?,
    val favorite: Boolean
)

@Serializable
data class SkillCard(
    val skillId: String,
    val skillDef: SkillDef,
    val instance: SkillInstance?,
    val isOwned: Boolean,
    val isEquipped: Boolean,
    val canUnlock: Boolean,
    val unlockCost: UnlockCost?,
    val previewData: SkillPreviewData
) {
    fun getRarityColor(): String {
        return when (skillDef.rarity) {
            SkillRarity.COMMON -> "#A0A0A0"
            SkillRarity.UNCOMMON -> "#4CAF50"
            SkillRarity.RARE -> "#2196F3"
            SkillRarity.EPIC -> "#9C27B0"
            SkillRarity.LEGENDARY -> "#FFD700"
        }
    }
    
    fun getTypeIcon(): String {
        return when (skillDef.type) {
            SkillType.PASSIVE -> "passive_icon"
            SkillType.ACTIVE -> "active_icon"
            SkillType.COSMETIC -> "cosmetic_icon"
        }
    }
    
    fun getCategoryIcon(): String {
        return when (skillDef.category) {
            SkillCategory.COMBAT -> "combat_icon"
            SkillCategory.SUPPORT -> "support_icon"
            SkillCategory.UTILITY -> "utility_icon"
            SkillCategory.SOCIAL -> "social_icon"
            SkillCategory.LUCK -> "luck_icon"
            SkillCategory.DEFENSE -> "defense_icon"
            SkillCategory.OFFENSE -> "offense_icon"
            SkillCategory.SPECIAL -> "special_icon"
        }
    }
}

@Serializable
data class SkillPreviewData(
    val description: String,
    val effects: List<String>,
    val cooldown: String,
    val requirements: List<String>,
    val rarity: SkillRarity,
    val type: SkillType
)

@Serializable
data class SkillCollection(
    val ownedSkills: List<SkillInstance>,
    val equippedSkills: List<SkillInstance>,
    val unlockedSkills: List<SkillInstance>,
    val lockedSkills: List<String>,
    val totalSkills: Int,
    val collectionProgress: Float
) {
    fun getSkillsByType(type: SkillType): List<SkillInstance> {
        return ownedSkills.filter { 
            SkillRegistry.getSkillById(it.skillId)?.type == type 
        }
    }
    
    fun getSkillsByRarity(rarity: SkillRarity): List<SkillInstance> {
        return ownedSkills.filter { 
            SkillRegistry.getSkillById(it.skillId)?.rarity == rarity 
        }
    }
    
    fun getSkillsByCategory(category: SkillCategory): List<SkillInstance> {
        return ownedSkills.filter { 
            SkillRegistry.getSkillById(it.skillId)?.category == category 
        }
    }
    
    fun getEquippedCount(): Int {
        return equippedSkills.size
    }
    
    fun getMaxEquipped(): Int {
        return 6
    }
    
    fun canEquipMore(): Boolean {
        return getEquippedCount() < getMaxEquipped()
    }
}

@Serializable
data class SkillStats(
    val totalSkills: Int,
    val ownedSkills: Int,
    val unlockedSkills: Int,
    val equippedSkills: Int,
    val skillsByType: Map<SkillType, Int>,
    val skillsByRarity: Map<SkillRarity, Int>,
    val skillsByCategory: Map<SkillCategory, Int>,
    val totalUses: Int,
    val averageLevel: Float,
    val totalExperience: Long
) {
    fun getOwnershipRate(): Float {
        return if (totalSkills > 0) {
            (ownedSkills.toFloat() / totalSkills.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getUnlockRate(): Float {
        return if (ownedSkills > 0) {
            (unlockedSkills.toFloat() / ownedSkills.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getEquippedRate(): Float {
        return if (ownedSkills > 0) {
            (equippedSkills.toFloat() / ownedSkills.toFloat()) * 100f
        } else {
            0f
        }
    }
}

object SkillRegistry {
    private val skills = mutableMapOf<String, SkillDef>()
    
    fun registerSkill(skill: SkillDef) {
        skills[skill.id] = skill
    }
    
    fun getSkillById(id: String): SkillDef? {
        return skills[id]
    }
    
    fun getAllSkills(): List<SkillDef> {
        return skills.values.toList()
    }
    
    fun getSkillsByType(type: SkillType): List<SkillDef> {
        return skills.values.filter { it.type == type }
    }
    
    fun getSkillsByRarity(rarity: SkillRarity): List<SkillDef> {
        return skills.values.filter { it.rarity == rarity }
    }
    
    fun getSkillsByCategory(category: SkillCategory): List<SkillDef> {
        return skills.values.filter { it.category == category }
    }
    
    fun clear() {
        skills.clear()
    }
}