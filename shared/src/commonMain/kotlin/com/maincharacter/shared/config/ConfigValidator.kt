package com.maincharacter.shared.config.legacy

import com.maincharacter.shared.model.RewardBundle

class ConfigValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    )

    fun validateRewardBundle(bundle: RewardBundle): ValidationResult {
        val errors = mutableListOf<String>()
        bundle.currencies.forEach { (currencyId, amount) ->
            if (amount < 0) {
                errors += "Currency amount must be >= 0: $currencyId=$amount"
            }
        }
        bundle.items.forEach { item ->
            if (item.count < 1) {
                errors += "Item count must be >= 1: ${item.itemId}=${item.count}"
            }
        }
        bundle.buffs.forEach { buff ->
            if (buff.durationSec < 0) {
                errors += "Buff duration must be >= 0: ${buff.buffId}=${buff.durationSec}"
            }
        }
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateTaskDef(task: Any): ValidationResult =
        ValidationResult(isValid = true)

    fun validateEventDef(event: Any): ValidationResult =
        ValidationResult(isValid = true)

    fun validateGachaPool(pool: LegacyGachaPoolDef): ValidationResult {
        val errors = mutableListOf<String>()
        if (pool.id.isBlank()) errors += "Pool id cannot be empty"
        if (pool.rows.isEmpty()) errors += "Pool must have at least one row"
        pool.rows.forEach { row ->
            if (row.weight <= 0) {
                errors += "Row weight must be > 0: ${row.id}"
            }
            if (row.rewardBundleId.isBlank()) {
                errors += "Row rewardBundleId cannot be empty: ${row.id}"
            }
        }
        return ValidationResult(errors.isEmpty(), errors)
    }
}
