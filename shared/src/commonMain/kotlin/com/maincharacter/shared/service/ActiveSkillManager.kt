package com.maincharacter.shared.service

import com.maincharacter.shared.model.SkillInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class ActiveSkillManager(
    private val skillRegistry: SkillRegistry,
    private val eventRegistry: EventRegistry
) {
    private val _activeSkills = MutableStateFlow<List<SkillInstance>>(emptyList())
    val activeSkills: StateFlow<List<SkillInstance>> = _activeSkills

    fun activateSkill(
        skillInstance: SkillInstance,
        context: SkillActivationContext
    ): SkillActivationResult {
        val skillDef = skillRegistry.getSkillById(skillInstance.skillId)
            ?: return SkillActivationResult(false, "Skill not found", emptyList())

        _activeSkills.value = (_activeSkills.value.filterNot { it.skillId == skillInstance.skillId } + skillInstance)

        return SkillActivationResult(
            success = true,
            error = null,
            effects = listOf(
                SkillEffectResult(
                    skillId = skillInstance.skillId,
                    effectType = null,
                    value = 0f,
                    description = "Activated ${skillDef.name}"
                )
            )
        )
    }
}

@Serializable
data class SkillActivationContext(
    val eventId: String? = null,
    val choiceId: String? = null,
    val userId: String = "",
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class SkillEffectResult(
    val skillId: String,
    val effectType: String? = null,
    val value: Float = 0f,
    val description: String
)

@Serializable
data class SkillActivationResult(
    val success: Boolean,
    val error: String?,
    val effects: List<SkillEffectResult>
)
