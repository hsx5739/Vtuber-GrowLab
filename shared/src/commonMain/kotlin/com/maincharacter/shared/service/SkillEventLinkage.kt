package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class SkillEventLinkage(
    private val skillRegistry: SkillRegistry,
    private val eventRegistry: EventRegistry,
    private val passiveSkillManager: PassiveSkillManager,
    private val activeSkillManager: ActiveSkillManager
) {
    
    private val _eventSkillLinks = MutableStateFlow<Map<String, EventSkillLinks>>(emptyMap())
    val eventSkillLinks: StateFlow<Map<String, EventSkillLinks>> = _eventSkillLinks
    
    private val _skillEventLinks = MutableStateFlow<Map<String, SkillEventLinks>>(emptyMap())
    val skillEventLinks: StateFlow<Map<String, SkillEventLinks>> = _skillEventLinks
    
    private val _activeLinkages = MutableStateFlow<List<ActiveLinkage>>(emptyList())
    val activeLinkages: StateFlow<List<ActiveLinkage>> = _activeLinkages
    
    private val _linkageStats = MutableStateFlow(LinkageStats())
    val linkageStats: StateFlow<LinkageStats> = _linkageStats
    
    fun initializeSkillEventLinks() {
        val eventLinks = mutableMapOf<String, EventSkillLinks>()
        val skillLinks = mutableMapOf<String, SkillEventLinks>()
        
        val allSkills = skillRegistry.getAllSkills()
        val allEvents = eventRegistry.getAllEvents()
        
        allSkills.forEach { skillDef ->
            val linkedEventIds = getLinkedEventIds(skillDef)
            
            skillLinks[skillDef.id] = SkillEventLinks(
                skillId = skillDef.id,
                skillName = skillDef.name,
                skillType = skillDef.type,
                linkedEventIds = linkedEventIds,
                linkedEventsCount = linkedEventIds.size,
                linkages = buildSkillLinkages(skillDef, linkedEventIds)
            )
            
            linkedEventIds.forEach { eventId ->
                val currentLinks = eventLinks[eventId] ?: EventSkillLinks(
                    eventId = eventId,
                    eventName = eventRegistry.getEventById(eventId)?.name ?: eventId,
                    linkedSkillIds = emptySet(),
                    linkedSkillsCount = 0,
                    linkages = emptyList()
                )
                
                val updatedLinks = currentLinks.copy(
                    linkedSkillIds = currentLinks.linkedSkillIds + skillDef.id,
                    linkedSkillsCount = currentLinks.linkedSkillsCount + 1,
                    linkages = currentLinks.linkages + buildLinkage(skillDef, eventId)
                )
                
                eventLinks[eventId] = updatedLinks
            }
        }
        
        _eventSkillLinks.value = eventLinks
        _skillEventLinks.value = skillLinks
    }
    
    private fun getLinkedEventIds(skillDef: SkillDef): Set<String> {
        val linkedEventIds = mutableSetOf<String>()
        
        skillDef.effects.forEach { effect ->
            when (effect.target) {
                EffectTarget.EVENT -> {
                    val eventId = effect.metadata["eventId"]
                    if (eventId != null) {
                        linkedEventIds.add(eventId)
                    }
                }
                EffectTarget.GLOBAL -> {
                    eventRegistry.getAllEvents().forEach { eventDef ->
                        linkedEventIds.add(eventDef.id)
                    }
                }
                else -> {}
            }
        }
        
        return linkedEventIds
    }
    
    private fun buildSkillLinkages(skillDef: SkillDef, eventIds: Set<String>): List<Linkage> {
        return eventIds.map { eventId ->
            buildLinkage(skillDef, eventId)
        }
    }
    
    private fun buildLinkage(skillDef: SkillDef, eventId: String): Linkage {
        val eventDef = eventRegistry.getEventById(eventId)
        
        val applicableEffects = skillDef.effects.filter { effect ->
            when (effect.target) {
                EffectTarget.GLOBAL -> true
                EffectTarget.EVENT -> effect.metadata["eventId"] == eventId
                else -> false
            }
        }
        
        return Linkage(
            skillId = skillDef.id,
            skillName = skillDef.name,
            skillType = skillDef.type,
            eventId = eventId,
            eventName = eventDef?.name ?: eventId,
            effects = applicableEffects,
            isActive = false,
            activationTime = 0L
        )
    }
    
    fun activateLinkage(skillId: String, eventId: String): LinkageActivationResult {
        val skillDef = skillRegistry.getSkillById(skillId)
        if (skillDef == null) {
            return LinkageActivationResult(
                success = false,
                error = "Skill not found"
            )
        }
        
        val eventDef = eventRegistry.getEventById(eventId)
        if (eventDef == null) {
            return LinkageActivationResult(
                success = false,
                error = "Event not found"
            )
        }
        
        val skillLinks = _skillEventLinks.value[skillId]
        if (skillLinks == null || eventId !in skillLinks.linkedEventIds) {
            return LinkageActivationResult(
                success = false,
                error = "Skill and event are not linked"
            )
        }
        
        val linkage = buildLinkage(skillDef, eventId)
        val activeLinkage = ActiveLinkage(
            linkage = linkage,
            activationTime = System.currentTimeMillis(),
            context = LinkageContext(
                skillId = skillId,
                eventId = eventId,
                userId = "",
                sessionId = ""
            )
        )
        
        val currentActive = _activeLinkages.value.toMutableList()
        currentActive.add(activeLinkage)
        _activeLinkages.value = currentActive
        
        updateStats(skillDef, eventDef, true)
        
        return LinkageActivationResult(
            success = true,
            error = null
        )
    }
    
    fun deactivateLinkage(skillId: String, eventId: String): LinkageDeactivationResult {
        val currentActive = _activeLinkages.value
        val activeLinkage = currentActive.find { 
            it.linkage.skillId == skillId && it.linkage.eventId == eventId 
        }
        
        if (activeLinkage == null) {
            return LinkageDeactivationResult(
                success = false,
                error = "Linkage is not active"
            )
        }
        
        val updatedActive = currentActive.filterNot { 
            it.linkage.skillId == skillId && it.linkage.eventId == eventId 
        }
        _activeLinkages.value = updatedActive
        
        val skillDef = skillRegistry.getSkillById(skillId)
        val eventDef = eventRegistry.getEventById(eventId)
        
        if (skillDef != null && eventDef != null) {
            updateStats(skillDef, eventDef, false)
        }
        
        return LinkageDeactivationResult(
            success = true,
            error = null
        )
    }
    
    fun getEventSkillLinks(eventId: String): EventSkillLinks? {
        return _eventSkillLinks.value[eventId]
    }
    
    fun getSkillEventLinks(skillId: String): SkillEventLinks? {
        return _skillEventLinks.value[skillId]
    }
    
    fun getAllEventSkillLinks(): Map<String, EventSkillLinks> {
        return _eventSkillLinks.value
    }
    
    fun getAllSkillEventLinks(): Map<String, SkillEventLinks> {
        return _skillEventLinks.value
    }
    
    fun getActiveLinkages(): List<ActiveLinkage> {
        return _activeLinkages.value
    }
    
    fun getActiveLinkagesForEvent(eventId: String): List<ActiveLinkage> {
        return _activeLinkages.value.filter { it.linkage.eventId == eventId }
    }
    
    fun getActiveLinkagesForSkill(skillId: String): List<ActiveLinkage> {
        return _activeLinkages.value.filter { it.linkage.skillId == skillId }
    }
    
    fun getActiveLinkagesByType(skillType: SkillType): List<ActiveLinkage> {
        return _activeLinkages.value.filter { it.linkage.skillType == skillType }
    }
    
    fun applyPassiveSkillEffects(eventId: String, baseWeight: Float): Float {
        val passiveLinkages = getActiveLinkagesForEvent(eventId)
            .filter { it.linkage.skillType == SkillType.PASSIVE }
        
        var modifiedWeight = baseWeight
        
        passiveLinkages.forEach { activeLinkage ->
            val skillDef = skillRegistry.getSkillById(activeLinkage.linkage.skillId)
            if (skillDef != null) {
                val weightModifier = passiveSkillManager.getWeightModifier(eventId)
                modifiedWeight *= weightModifier
            }
        }
        
        return modifiedWeight
    }
    
    fun applyActiveSkillEffects(
        eventId: String,
        choiceId: String?,
        context: SkillActivationContext
    ): List<SkillEffectResult> {
        val activeLinkages = getActiveLinkagesForEvent(eventId)
            .filter { it.linkage.skillType == SkillType.ACTIVE }
        
        val effects = mutableListOf<SkillEffectResult>()
        
        activeLinkages.forEach { activeLinkage ->
            val skillDef = skillRegistry.getSkillById(activeLinkage.linkage.skillId)
            if (skillDef != null) {
                val activationResult = activeSkillManager.activateSkill(
                    skillInstance = SkillInstance(
                        id = "",
                        skillId = skillDef.id,
                        level = 1,
                        experience = 0,
                        isUnlocked = true,
                        isEquipped = true,
                        unlockTime = 0L,
                        lastUseTime = 0L,
                        useCount = 0,
                        cooldownEndTime = 0L,
                        charges = skillDef.cooldown.charges,
                        metadata = SkillInstanceMetadata(
                            userId = "",
                            unlockSource = "",
                            customName = null,
                            customNotes = null,
                            favorite = false
                        )
                    ),
                    context = context
                )
                
                if (activationResult.success) {
                    effects.addAll(activationResult.effects)
                }
            }
        }
        
        return effects
    }
    
    fun getEventsAffectedBySkill(skillId: String): List<String> {
        val skillLinks = _skillEventLinks.value[skillId]
        return skillLinks?.linkedEventIds?.toList() ?: emptyList()
    }
    
    fun getSkillsAffectingEvent(eventId: String): List<String> {
        val eventLinks = _eventSkillLinks.value[eventId]
        return eventLinks?.linkedSkillIds?.toList() ?: emptyList()
    }
    
    fun getPassiveSkillsAffectingEvent(eventId: String): List<String> {
        val eventLinks = _eventSkillLinks.value[eventId]
        return eventLinks?.linkages
            ?.filter { it.skillType == SkillType.PASSIVE }
            ?.map { it.skillId }
            ?: emptyList()
    }
    
    fun getActiveSkillsAffectingEvent(eventId: String): List<String> {
        val eventLinks = _eventSkillLinks.value[eventId]
        return eventLinks?.linkages
            ?.filter { it.skillType == SkillType.ACTIVE }
            ?.map { it.skillId }
            ?: emptyList()
    }
    
    fun getLinkageStats(): LinkageStats {
        return _linkageStats.value
    }
    
    fun getLinkageSummary(): LinkageSummary {
        val eventLinks = _eventSkillLinks.value
        val skillLinks = _skillEventLinks.value
        val activeLinkages = _activeLinkages.value
        
        val totalEventLinks = eventLinks.size
        val totalSkillLinks = skillLinks.size
        val totalActiveLinkages = activeLinkages.size
        
        val totalLinkages = skillLinks.values.sumOf { it.linkedEventsCount }
        val activePassiveLinkages = activeLinkages.count { it.linkage.skillType == SkillType.PASSIVE }
        val activeActiveLinkages = activeLinkages.count { it.linkage.skillType == SkillType.ACTIVE }
        
        val mostLinkedEvent = eventLinks.maxByOrNull { it.value.linkedSkillsCount }?.key
        val mostLinkedSkill = skillLinks.maxByOrNull { it.value.linkedEventsCount }?.key
        
        return LinkageSummary(
            totalEventLinks = totalEventLinks,
            totalSkillLinks = totalSkillLinks,
            totalLinkages = totalLinkages,
            totalActiveLinkages = totalActiveLinkages,
            activePassiveLinkages = activePassiveLinkages,
            activeActiveLinkages = activeActiveLinkages,
            mostLinkedEvent = mostLinkedEvent,
            mostLinkedSkill = mostLinkedSkill,
            averageLinksPerEvent = if (totalEventLinks > 0) {
                totalLinkages.toFloat() / totalEventLinks.toFloat()
            } else {
                0f
            },
            averageLinksPerSkill = if (totalSkillLinks > 0) {
                totalLinkages.toFloat() / totalSkillLinks.toFloat()
            } else {
                0f
            }
        )
    }
    
    fun clearAllLinkages() {
        _eventSkillLinks.value = emptyMap()
        _skillEventLinks.value = emptyMap()
        _activeLinkages.value = emptyList()
        _linkageStats.value = LinkageStats()
    }
    
    private fun updateStats(skillDef: SkillDef, eventDef: EventDef, isActive: Boolean) {
        val current = _linkageStats.value
        
        val updated = if (isActive) {
            current.copy(
                totalActivations = current.totalActivations + 1,
                skillActivations = current.skillActivations.toMutableMap().apply {
                    this[skillDef.id] = (this[skillDef.id] ?: 0) + 1
                },
                eventActivations = current.eventActivations.toMutableMap().apply {
                    this[eventDef.id] = (this[eventDef.id] ?: 0) + 1
                },
                typeActivations = current.typeActivations.toMutableMap().apply {
                    this[skillDef.type] = (this[skillDef.type] ?: 0) + 1
                }
            )
        } else {
            current.copy(
                totalDeactivations = current.totalDeactivations + 1,
                skillDeactivations = current.skillDeactivations.toMutableMap().apply {
                    this[skillDef.id] = (this[skillDef.id] ?: 0) + 1
                },
                eventDeactivations = current.eventDeactivations.toMutableMap().apply {
                    this[eventDef.id] = (this[eventDef.id] ?: 0) + 1
                }
            )
        }
        
        _linkageStats.value = updated
    }
    
    fun getMostActivatedSkill(): String? {
        return _linkageStats.value.skillActivations.maxByOrNull { it.value }?.key
    }
    
    fun getMostActivatedEvent(): String? {
        return _linkageStats.value.eventActivations.maxByOrNull { it.value }?.key
    }
    
    fun getMostActivatedType(): SkillType? {
        return _linkageStats.value.typeActivations.maxByOrNull { it.value }?.key
    }
}

@Serializable
data class EventSkillLinks(
    val eventId: String,
    val eventName: String,
    val linkedSkillIds: Set<String>,
    val linkedSkillsCount: Int,
    val linkages: List<Linkage>
) {
    fun getPassiveSkills(): List<Linkage> {
        return linkages.filter { it.skillType == SkillType.PASSIVE }
    }
    
    fun getActiveSkills(): List<Linkage> {
        return linkages.filter { it.skillType == SkillType.ACTIVE }
    }
    
    fun getCosmeticSkills(): List<Linkage> {
        return linkages.filter { it.skillType == SkillType.COSMETIC }
    }
}

@Serializable
data class SkillEventLinks(
    val skillId: String,
    val skillName: String,
    val skillType: SkillType,
    val linkedEventIds: Set<String>,
    val linkedEventsCount: Int,
    val linkages: List<Linkage>
) {
    fun getWeightModifierEvents(): List<String> {
        return linkages
            .filter { linkage ->
                linkage.effects.any { it.type == SkillEffectType.WEIGHT_MODIFIER }
            }
            .map { it.eventId }
    }
    
    fun getProbabilityBoostEvents(): List<String> {
        return linkages
            .filter { linkage ->
                linkage.effects.any { it.type == SkillEffectType.PROBABILITY_BOOST }
            }
            .map { it.eventId }
    }
    
    fun getCostReductionEvents(): List<String> {
        return linkages
            .filter { linkage ->
                linkage.effects.any { it.type == SkillEffectType.COST_REDUCTION }
            }
            .map { it.eventId }
    }
}

@Serializable
data class Linkage(
    val skillId: String,
    val skillName: String,
    val skillType: SkillType,
    val eventId: String,
    val eventName: String,
    val effects: List<SkillEffect>,
    val isActive: Boolean,
    val activationTime: Long
) {
    fun hasEffect(effectType: SkillEffectType): Boolean {
        return effects.any { it.type == effectType }
    }
    
    fun getEffectsByType(effectType: SkillEffectType): List<SkillEffect> {
        return effects.filter { it.type == effectType }
    }
}

@Serializable
data class ActiveLinkage(
    val linkage: Linkage,
    val activationTime: Long,
    val context: LinkageContext
) {
    fun getDuration(): Long {
        return System.currentTimeMillis() - activationTime
    }
    
    fun getDurationSeconds(): Float {
        return getDuration() / 1000f
    }
    
    fun getDurationMinutes(): Float {
        return getDuration() / 60000f
    }
}

@Serializable
data class LinkageContext(
    val skillId: String,
    val eventId: String,
    val userId: String,
    val sessionId: String
)

@Serializable
data class LinkageActivationResult(
    val success: Boolean,
    val error: String?
)

@Serializable
data class LinkageDeactivationResult(
    val success: Boolean,
    val error: String?
)

@Serializable
data class LinkageStats(
    val totalActivations: Int = 0,
    val totalDeactivations: Int = 0,
    val skillActivations: Map<String, Int> = emptyMap(),
    val skillDeactivations: Map<String, Int> = emptyMap(),
    val eventActivations: Map<String, Int> = emptyMap(),
    val eventDeactivations: Map<String, Int> = emptyMap(),
    val typeActivations: Map<SkillType, Int> = emptyMap()
) {
    fun getActivationRate(): Float {
        val total = totalActivations + totalDeactivations
        return if (total > 0) {
            (totalActivations.toFloat() / total.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getMostActivatedSkill(): String? {
        return skillActivations.maxByOrNull { it.value }?.key
    }
    
    fun getMostActivatedEvent(): String? {
        return eventActivations.maxByOrNull { it.value }?.key
    }
    
    fun getMostActivatedType(): SkillType? {
        return typeActivations.maxByOrNull { it.value }?.key
    }
}

@Serializable
data class LinkageSummary(
    val totalEventLinks: Int,
    val totalSkillLinks: Int,
    val totalLinkages: Int,
    val totalActiveLinkages: Int,
    val activePassiveLinkages: Int,
    val activeActiveLinkages: Int,
    val mostLinkedEvent: String?,
    val mostLinkedSkill: String?,
    val averageLinksPerEvent: Float,
    val averageLinksPerSkill: Float
) {
    fun getActivationRate(): Float {
        return if (totalLinkages > 0) {
            (totalActiveLinkages.toFloat() / totalLinkages.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getPassiveActivationRate(): Float {
        return if (activePassiveLinkages + activeActiveLinkages > 0) {
            (activePassiveLinkages.toFloat() / (activePassiveLinkages + activeActiveLinkages).toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getActiveActivationRate(): Float {
        return if (activePassiveLinkages + activeActiveLinkages > 0) {
            (activeActiveLinkages.toFloat() / (activePassiveLinkages + activeActiveLinkages).toFloat()) * 100f
        } else {
            0f
        }
    }
}
