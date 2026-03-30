package com.maincharacter.shared.config

import com.maincharacter.shared.model.GachaPoolDef
import com.maincharacter.shared.model.RewardBundle
import com.maincharacter.shared.model.SkinRarity
import com.maincharacter.shared.model.StatKey
import com.maincharacter.shared.model.legacy.EventDef
import com.maincharacter.shared.model.legacy.TaskDef
import com.maincharacter.shared.model.legacy.TaskType

class ConfigValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    )

    fun validateRewardBundle(bundle: RewardBundle): ValidationResult {
        val errors = mutableListOf<String>()

        bundle.currencies.forEach { (currencyId, amount) ->
            if (amount < 0) {
                errors.add("Currency amount must be >= 0: $currencyId=$amount")
            }
        }

        bundle.statDelta.forEach { (statKey, _) ->
            if (!isValidStatKey(statKey)) {
                errors.add("Invalid stat key: $statKey")
            }
        }

        bundle.items.forEach { item ->
            if (item.count < 1) {
                errors.add("Item count must be >= 1: ${item.itemId}=${item.count}")
            }
        }

        bundle.buffs.forEach { buff ->
            if (buff.durationSec < 0) {
                errors.add("Buff duration must be >= 0: ${buff.buffId}=${buff.durationSec}")
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateTaskDef(task: TaskDef): ValidationResult {
        val errors = mutableListOf<String>()

        if (task.rewardBundleId.isBlank()) {
            errors.add("Task must have rewardBundleId")
        }

        if (!isValidTaskType(task.taskType)) {
            errors.add("Invalid task type: ${task.taskType}")
        }

        if (task.maxCompletionsPerDay != null && task.maxCompletionsPerDay < 1) {
            errors.add("maxCompletionsPerDay must be >= 1")
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateEventDef(event: EventDef): ValidationResult {
        val errors = mutableListOf<String>()

        if (event.branches.isEmpty()) {
            errors.add("Event must have at least one branch")
        }

        event.branches.forEach { branch ->
            if (branch.rewardBundleId.isBlank()) {
                errors.add("Branch must have rewardBundleId: ${branch.optionId}")
            }

            if (branch.energyCost != null && branch.energyCost < 0) {
                errors.add("Energy cost must be >= 0: ${branch.optionId}")
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateGachaPool(pool: GachaPoolDef): ValidationResult {
        val errors = mutableListOf<String>()

        if (pool.rows.isEmpty()) {
            errors.add("Pool must have at least one row")
        }

        pool.rows.forEach { row ->
            if (row.weight <= 0) {
                errors.add("Row weight must be > 0: ${row.rowId}")
            }

            if (row.skinIds.isEmpty()) {
                errors.add("Row must contain at least one skinId: ${row.rowId}")
            }

            if (row.minGuaranteedCount < 0) {
                errors.add("minGuaranteedCount must be >= 0: ${row.rowId}")
            }

            if (row.maxGuaranteedCount < row.minGuaranteedCount) {
                errors.add("maxGuaranteedCount must be >= minGuaranteedCount: ${row.rowId}")
            }
        }

        val pityRules = pool.pityRules
        if (pityRules.rarePullsMax < 1) {
            errors.add("rarePullsMax must be >= 1")
        }
        if (pityRules.superRarePullsMax < 1) {
            errors.add("superRarePullsMax must be >= 1")
        }
        if (pityRules.ultraRarePullsMax < 1) {
            errors.add("ultraRarePullsMax must be >= 1")
        }
        if (!isAscendingRarity(pityRules.rareRarity, pityRules.superRareRarity, pityRules.ultraRareRarity)) {
            errors.add("Pity rarity order must ascend from rare to ultra rare")
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    private fun isValidStatKey(key: StatKey): Boolean {
        return key in listOf(
            StatKey.FORTUNE,
            StatKey.VITALITY,
            StatKey.MOOD,
            StatKey.BOND,
            StatKey.FOCUS
        )
    }

    private fun isValidTaskType(type: TaskType): Boolean {
        return type in listOf(
            TaskType.A,
            TaskType.B,
            TaskType.C,
            TaskType.D,
            TaskType.E,
            TaskType.F,
            TaskType.G,
            TaskType.H
        )
    }

    private fun isAscendingRarity(
        rare: SkinRarity,
        superRare: SkinRarity,
        ultraRare: SkinRarity
    ): Boolean {
        return rare.tier < superRare.tier && superRare.tier < ultraRare.tier
    }
}
